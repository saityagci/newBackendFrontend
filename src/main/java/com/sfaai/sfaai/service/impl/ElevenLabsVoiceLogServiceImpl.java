package com.sfaai.sfaai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.config.ElevenLabsConfig;
import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.service.ElevenLabsVoiceLogService;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ElevenLabs voice log service
 * Handles synchronization from ElevenLabs API and audio streaming
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsVoiceLogServiceImpl implements ElevenLabsVoiceLogService {

    private final VoiceLogRepository voiceLogRepository;
    private final VoiceLogService voiceLogService;
    private final VoiceLogMapper voiceLogMapper;
    private final ElevenLabsConfig elevenLabsConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ElevenLabsAssistantRepository elevenLabsAssistantRepository;

    @Override
    public List<VoiceLogDTO> getAllElevenLabsVoiceLogs() {
        log.debug("Fetching all ElevenLabs voice logs from database");
        List<VoiceLog> voiceLogs = voiceLogRepository.findByProvider(VoiceLog.Provider.ELEVENLABS);
        return voiceLogs.stream()
                .map(voiceLogMapper::toDto)
                .toList();
    }

    @Override
    public Page<VoiceLogDTO> getElevenLabsVoiceLogs(Pageable pageable) {
        log.debug("Fetching ElevenLabs voice logs with pagination: {}", pageable);
        Page<VoiceLog> voiceLogs = voiceLogRepository.findByProvider(VoiceLog.Provider.ELEVENLABS, pageable);
        return voiceLogs.map(voiceLogMapper::toDto);
    }

    @Override
    public VoiceLogDTO getElevenLabsVoiceLogByExternalCallId(String externalCallId) {
        log.debug("Fetching ElevenLabs voice log by external call ID: {}", externalCallId);
        Optional<VoiceLog> voiceLog = voiceLogRepository.findByExternalCallId(externalCallId);
        
        if (voiceLog.isPresent() && voiceLog.get().getProvider() == VoiceLog.Provider.ELEVENLABS) {
            return voiceLogMapper.toDto(voiceLog.get());
        }
        
        return null;
    }

    @Override
    public Resource getElevenLabsVoiceLogAudio(String externalCallId) {
        log.info("Fetching audio for ElevenLabs voice log: {}", externalCallId);
        
        // First verify the voice log exists and is from ElevenLabs
        Optional<VoiceLog> voiceLog = voiceLogRepository.findByExternalCallId(externalCallId);
        if (voiceLog.isEmpty() || voiceLog.get().getProvider() != VoiceLog.Provider.ELEVENLABS) {
            log.warn("ElevenLabs voice log not found: {}", externalCallId);
            return null;
        }

        try {
            // Fetch audio from ElevenLabs API
            String audioUrl = buildElevenLabsAudioUrl(externalCallId);
            HttpHeaders headers = createElevenLabsHeaders();
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                audioUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched audio for ElevenLabs voice log: {}", externalCallId);
                return new ByteArrayResource(response.getBody());
            } else {
                log.warn("Failed to fetch audio for ElevenLabs voice log: {}, status: {}", 
                        externalCallId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching audio for ElevenLabs voice log: {}", externalCallId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public SyncSummary syncVoiceLogsFromElevenLabs() {
        log.info("Starting ElevenLabs voice logs synchronization");
        long startTime = System.currentTimeMillis();
        
        List<String> updatedIds = new ArrayList<>();
        List<String> skippedIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        int fetched = 0;
        int updated = 0;
        int skipped = 0;
        int errors = 0;

        try {
            // Fetch all conversations from ElevenLabs
            List<JsonNode> conversations = fetchAllConversations();
            fetched = conversations.size();
            log.info("Fetched {} conversations from ElevenLabs API", fetched);

            // Process each conversation
            for (JsonNode conversation : conversations) {
                try {
                    String conversationId = conversation.get("conversation_id").asText();
                    log.debug("Processing conversation: {}", conversationId);

                    // Check if this conversation already exists
                    Optional<VoiceLog> existingLog = voiceLogRepository.findByExternalCallId(conversationId);
                    
                    VoiceLogCreateDTO dto = createVoiceLogDtoFromConversation(conversation);
                    if (dto == null) {
                        log.warn("Skipping conversation {} due to missing agent/client assignment.", conversationId);
                        skippedIds.add(conversationId);
                        skipped++;
                        continue;
                    }

                    if (existingLog.isPresent()) {
                        // Update existing log
                        VoiceLogDTO updatedLog = voiceLogService.createVoiceLog(dto);
                        updatedIds.add(conversationId);
                        updated++;
                        log.debug("Updated existing voice log for conversation: {}", conversationId);
                    } else {
                        // Create new log
                        VoiceLogDTO newLog = voiceLogService.createVoiceLog(dto);
                        updatedIds.add(conversationId);
                        updated++;
                        log.debug("Created new voice log for conversation: {}", conversationId);
                    }
                } catch (Exception e) {
                    String conversationId = conversation.has("conversation_id") ? 
                            conversation.get("conversation_id").asText() : "unknown";
                    log.error("Error processing conversation: {}", conversationId, e);
                    errorIds.add(conversationId);
                    errors++;
                }
            }

        } catch (Exception e) {
            log.error("Error during ElevenLabs voice logs synchronization", e);
            errors++;
        }

        long durationMs = System.currentTimeMillis() - startTime;
        SyncSummary summary = new SyncSummary(fetched, updated, skipped, errors, durationMs, 
                updatedIds, skippedIds, errorIds);

        log.info("ElevenLabs voice logs sync completed: {} fetched, {} updated, {} skipped, {} errors, {} ms",
                summary.fetched, summary.updated, summary.skipped, summary.errors, summary.durationMs);

        return summary;
    }

    @Override
    public SyncSummary manualSync() {
        log.info("Manual ElevenLabs voice logs sync triggered");
        return syncVoiceLogsFromElevenLabs();
    }

    /**
     * Fetch all conversations from ElevenLabs API
     */
    private List<JsonNode> fetchAllConversations() {
        List<JsonNode> allConversations = new ArrayList<>();
        String conversationsUrl = buildElevenLabsConversationsUrl();
        HttpHeaders headers = createElevenLabsHeaders();

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                conversationsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode conversations = response.getBody().get("conversations");
                if (conversations != null && conversations.isArray()) {
                    for (JsonNode conversation : conversations) {
                        allConversations.add(conversation);
                    }
                }
                log.debug("Fetched {} conversations from ElevenLabs API", allConversations.size());
            } else {
                log.error("Failed to fetch conversations from ElevenLabs API, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching conversations from ElevenLabs API", e);
        }

        return allConversations;
    }

    /**
     * Create VoiceLogCreateDTO from ElevenLabs conversation data
     */
    private VoiceLogCreateDTO createVoiceLogDtoFromConversation(JsonNode conversation) {
        String conversationId = conversation.get("conversation_id").asText();
        
        // Log the full conversation JSON for debugging
        log.debug("Processing ElevenLabs conversation JSON: {}", conversation.toString());

        // Try 'assistant_id', 'assistantId', and 'agent_id' for compatibility
        final String assistantId;
        if (conversation.has("assistant_id")) {
            assistantId = conversation.path("assistant_id").asText(null);
        } else if (conversation.has("assistantId")) {
            assistantId = conversation.path("assistantId").asText(null);
        } else if (conversation.has("agent_id")) {
            assistantId = conversation.path("agent_id").asText(null);
        } else {
            assistantId = null;
        }
        if (assistantId == null || assistantId.isEmpty()) {
            log.warn("No assistantId found in conversation {}. JSON: {}", conversationId, conversation.toString());
        }
        
        // Extract basic information
        String status = conversation.get("status").asText();
        
        // Extract timestamps
        LocalDateTime startedAt = null;
        LocalDateTime endedAt = null;
        
        if (conversation.has("created_at")) {
            startedAt = parseElevenLabsTimestamp(conversation.get("created_at").asText());
        }
        
        if (conversation.has("updated_at")) {
            endedAt = parseElevenLabsTimestamp(conversation.get("updated_at").asText());
        }

        // Extract transcript (flatten all messages)
        String transcript = null;
        if (conversation.has("transcript") && conversation.get("transcript").isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode turn : conversation.get("transcript")) {
                if (turn.has("message")) {
                    sb.append(turn.get("message").asText()).append("\n");
                }
            }
            transcript = sb.toString().trim();
        }

        // Extract phone number
        String phoneNumber = conversation.path("phone_number").asText(null);
        if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("conversation_initiation_client_data")) {
            JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
            if (dynVars.has("system__called_number")) {
                phoneNumber = dynVars.get("system__called_number").asText(null);
            }
        }

        // Extract duration in minutes
        Double durationMinutes = null;
        if (conversation.has("duration")) {
            durationMinutes = conversation.get("duration").asDouble();
        } else if (conversation.has("metadata") && conversation.get("metadata").has("call_duration_secs")) {
            durationMinutes = conversation.get("metadata").get("call_duration_secs").asDouble() / 60.0;
        } else if (conversation.has("call_duration_secs")) {
            durationMinutes = conversation.get("call_duration_secs").asDouble() / 60.0;
        } else if (conversation.has("conversation_initiation_client_data")) {
            JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
            if (dynVars.has("system__call_duration_secs")) {
                durationMinutes = dynVars.get("system__call_duration_secs").asDouble() / 60.0;
            }
        }

        // Store the full conversation data as JSON
        String rawData = conversation.toString();

        VoiceLogCreateDTO dto = new VoiceLogCreateDTO();
        dto.setExternalCallId(conversationId);
        dto.setProvider(VoiceLog.Provider.ELEVENLABS.name());
        dto.setStatus(mapElevenLabsStatus(status));
        dto.setStartedAt(startedAt);
        dto.setEndedAt(endedAt);
        dto.setTranscript(transcript);
        dto.setPhoneNumber(phoneNumber);
        dto.setDurationMinutes(durationMinutes);
        dto.setRawPayload(rawData);
        
        // Set assistant ID if available
        if (assistantId != null && !assistantId.isEmpty()) {
            dto.setExternalAgentId(assistantId);
            dto.setAssistantId(assistantId); // Set the required assistantId field
            // Look up ElevenLabsAssistant and assign agent/client if available
            elevenLabsAssistantRepository.findById(assistantId).ifPresentOrElse(assistant -> {
                if (assistant.getClient() != null) {
                    dto.setClientId(assistant.getClient().getId());
                }
                if (assistant.getAgent() != null) {
                    dto.setAgentId(assistant.getAgent().getId());
                }
            }, () -> {
                log.warn("No ElevenLabsAssistant found for assistantId {}. Skipping agent/client assignment.", assistantId);
            });
        }

        // If agentId or clientId is still null, log and skip
        if (dto.getClientId() == null || dto.getAgentId() == null) {
            log.warn("No client or agent assigned for ElevenLabs conversation {} (assistantId {}). Skipping log.", conversationId, assistantId);
            return null;
        }

        return dto;
    }

    /**
     * Map ElevenLabs status to VoiceLog status
     */
    private VoiceLog.Status mapElevenLabsStatus(String elevenLabsStatus) {
        if (elevenLabsStatus == null) {
            return VoiceLog.Status.INITIATED;
        }

        return switch (elevenLabsStatus.toLowerCase()) {
            case "completed" -> VoiceLog.Status.COMPLETED;
            case "failed" -> VoiceLog.Status.FAILED;
            case "cancelled" -> VoiceLog.Status.CANCELLED;
            case "in_progress", "active" -> VoiceLog.Status.IN_PROGRESS;
            case "ringing" -> VoiceLog.Status.RINGING;
            default -> VoiceLog.Status.INITIATED;
        };
    }

    /**
     * Parse ElevenLabs timestamp format
     */
    private LocalDateTime parseElevenLabsTimestamp(String timestamp) {
        try {
            // ElevenLabs uses ISO 8601 format
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse ElevenLabs timestamp: {}", timestamp, e);
            return null;
        }
    }

    /**
     * Build ElevenLabs conversations URL
     */
    private String buildElevenLabsConversationsUrl() {
        return UriComponentsBuilder
                .fromHttpUrl(elevenLabsConfig.getApiUrl())
                .path("/v1/convai/conversations")
                .build()
                .toUriString();
    }

    /**
     * Build ElevenLabs audio URL for a specific conversation
     */
    private String buildElevenLabsAudioUrl(String conversationId) {
        return UriComponentsBuilder
                .fromHttpUrl(elevenLabsConfig.getApiUrl())
                .path("/v1/convai/conversations/{conversationId}/audio")
                .buildAndExpand(conversationId)
                .toUriString();
    }

    /**
     * Create headers for ElevenLabs API requests
     */
    private HttpHeaders createElevenLabsHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("xi-api-key", elevenLabsConfig.getApiKey());
        return headers;
    }
} 