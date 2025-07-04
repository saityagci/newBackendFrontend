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
import com.sfaai.sfaai.service.AudioStorageService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Spliterators;

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
    private final AudioStorageService audioStorageService;

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
                        log.debug("Updated existing voice log for conversation: {} - transcript: {}", 
                                 conversationId, 
                                 updatedLog.getTranscript() != null ? "present (" + updatedLog.getTranscript().length() + " chars)" : "null");
                    } else {
                        // Create new log
                        VoiceLogDTO newLog = voiceLogService.createVoiceLog(dto);
                        updatedIds.add(conversationId);
                        updated++;
                        log.debug("Created new voice log for conversation: {} - transcript: {}", 
                                 conversationId, 
                                 newLog.getTranscript() != null ? "present (" + newLog.getTranscript().length() + " chars)" : "null");
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

    @Override
    public SyncSummary forceUpdateTranscriptFormat() {
        log.info("Starting forced update of transcript format for all ElevenLabs voice logs");
        
        List<VoiceLog> existingLogs = voiceLogRepository.findByProvider(VoiceLog.Provider.ELEVENLABS);
        int updatedCount = 0;
        int errorCount = 0;
        List<String> updatedIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        
        for (VoiceLog voiceLog : existingLogs) {
            try {
                String oldTranscript = voiceLog.getTranscript();
                if (oldTranscript != null && !oldTranscript.isEmpty()) {
                    // Convert [agent] to AI: and [user] to User:
                    String newTranscript = oldTranscript
                        .replaceAll("\\[agent\\]:", "AI:")
                        .replaceAll("\\[user\\]:", "User:")
                        .replaceAll("\\[agent\\]", "AI:")
                        .replaceAll("\\[user\\]", "User:");
                    
                    if (!oldTranscript.equals(newTranscript)) {
                        voiceLog.setTranscript(newTranscript);
                        voiceLogRepository.save(voiceLog);
                        updatedCount++;
                        updatedIds.add(voiceLog.getExternalCallId());
                        log.debug("Updated transcript format for voice log ID: {}", voiceLog.getId());
                    }
                }
            } catch (Exception e) {
                errorCount++;
                errorIds.add(voiceLog.getExternalCallId());
                log.error("Error updating transcript format for voice log ID {}: {}", voiceLog.getId(), e.getMessage());
            }
        }
        
        log.info("Force update completed: {} updated, {} errors", updatedCount, errorCount);
        return new SyncSummary(0, updatedCount, 0, errorCount, System.currentTimeMillis(), updatedIds, new ArrayList<>(), errorIds);
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
     * Fetch detailed conversation data including transcript from ElevenLabs API
     */
    private JsonNode fetchConversationDetails(String conversationId) {
        String detailsUrl = buildElevenLabsConversationDetailsUrl(conversationId);
        HttpHeaders headers = createElevenLabsHeaders();

        log.info("Fetching detailed conversation data from: {}", detailsUrl);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                detailsUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched details for conversation: {}", conversationId);
                log.debug("Detailed conversation response: {}", response.getBody().toString());
                
                // Log all available fields
                List<String> responseFieldNames = new ArrayList<>();
                response.getBody().fieldNames().forEachRemaining(responseFieldNames::add);
                log.info("Available fields in detailed conversation: {}", responseFieldNames);
                
                // Check if transcript field exists
                if (response.getBody().has("transcript")) {
                    JsonNode transcriptNode = response.getBody().get("transcript");
                    log.info("Transcript field found, type: {}, isArray: {}", 
                            transcriptNode.getNodeType(), transcriptNode.isArray());
                    if (transcriptNode.isArray()) {
                        log.info("Transcript array has {} elements", transcriptNode.size());
                    }
                } else {
                    log.warn("No transcript field found in detailed conversation response");
                }
                
                return response.getBody();
            } else {
                log.warn("Failed to fetch details for conversation: {}, status: {}", conversationId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching details for conversation: {}", conversationId, e);
        }

        return null;
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
        
        // Extract timestamps from basic conversation data
        LocalDateTime startedAt = null;
        LocalDateTime endedAt = null;
        
        // Check for start_time_unix_secs in basic conversation (new ElevenLabs format)
        if (conversation.has("start_time_unix_secs")) {
            long startTimeUnix = conversation.get("start_time_unix_secs").asLong();
            startedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneOffset.UTC);
            log.debug("Extracted startedAt from basic conversation start_time_unix_secs: {} -> {}", startTimeUnix, startedAt);
        }
        
        // Check for call_duration_secs in basic conversation
        if (startedAt != null && conversation.has("call_duration_secs")) {
            long durationSecs = conversation.get("call_duration_secs").asLong();
            endedAt = startedAt.plusSeconds(durationSecs);
            log.debug("Calculated endedAt from basic conversation: startedAt {} + {} seconds = {}", startedAt, durationSecs, endedAt);
        }
        
        // Fallback to metadata if not found in basic conversation
        if (startedAt == null && conversation.has("metadata")) {
            JsonNode metadata = conversation.get("metadata");
            
            // Extract start time from metadata.start_time_unix_secs
            if (metadata.has("start_time_unix_secs")) {
                long startTimeUnix = metadata.get("start_time_unix_secs").asLong();
                startedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneOffset.UTC);
                log.debug("Extracted startedAt from metadata.start_time_unix_secs: {} -> {}", startTimeUnix, startedAt);
            }
            
            // Extract end time by adding duration to start time
            if (startedAt != null && metadata.has("call_duration_secs")) {
                long durationSecs = metadata.get("call_duration_secs").asLong();
                endedAt = startedAt.plusSeconds(durationSecs);
                log.debug("Calculated endedAt from metadata: startedAt {} + {} seconds = {}", startedAt, durationSecs, endedAt);
            }
        }
        
        // Fallback to old timestamp fields if all else failed
        if (startedAt == null && conversation.has("created_at")) {
            startedAt = parseElevenLabsTimestamp(conversation.get("created_at").asText());
            log.debug("Fallback: extracted startedAt from created_at: {}", startedAt);
        }
        
        if (endedAt == null && conversation.has("updated_at")) {
            endedAt = parseElevenLabsTimestamp(conversation.get("updated_at").asText());
            log.debug("Fallback: extracted endedAt from updated_at: {}", endedAt);
        }

        // Extract transcript (detailed conversation + summary)
        String detailedTranscript = null;
        String transcriptSummary = null;
        JsonNode detailedConversation = null;
        
        // First try to get the transcript summary from analysis
        if (conversation.has("analysis") && conversation.get("analysis").has("transcript_summary")) {
            transcriptSummary = conversation.get("analysis").get("transcript_summary").asText();
            log.debug("Found transcript summary: {}", transcriptSummary);
        }
        
        // Try to extract detailed transcript from various sources
        if (conversation.has("transcript") && conversation.get("transcript").isArray()) {
            StringBuilder transcriptBuilder = new StringBuilder();
            JsonNode transcriptArray = conversation.get("transcript");
            log.debug("Found transcript array with {} elements", transcriptArray.size());
            
            for (JsonNode turn : transcriptArray) {
                if (turn.has("message")) {
                    String message = turn.get("message").asText();
                    String role = turn.has("role") ? turn.get("role").asText() : "unknown";
                    // Map role to AI/User format
                    String displayRole = "agent".equalsIgnoreCase(role) ? "AI" : "User";
                    transcriptBuilder.append(displayRole).append(": ").append(message).append("\n");
                    log.debug("Added transcript turn - role: {}, message: {}", displayRole, message);
                }
            }
            detailedTranscript = transcriptBuilder.toString().trim();
            log.debug("Extracted detailed transcript from 'transcript' array: {}", detailedTranscript);
        } else if (conversation.has("conversation_data") && conversation.get("conversation_data").has("transcript")) {
            // Try conversation_data.transcript
            JsonNode conversationData = conversation.get("conversation_data");
            if (conversationData.has("transcript") && conversationData.get("transcript").isArray()) {
                StringBuilder conversationDataBuilder = new StringBuilder();
                JsonNode transcriptArray = conversationData.get("transcript");
                log.debug("Found transcript array in conversation_data with {} elements", transcriptArray.size());
                
                for (JsonNode turn : transcriptArray) {
                    if (turn.has("message")) {
                        String message = turn.get("message").asText();
                        String role = turn.has("role") ? turn.get("role").asText() : "unknown";
                        // Map role to AI/User format
                        String displayRole = "agent".equalsIgnoreCase(role) ? "AI" : "User";
                        conversationDataBuilder.append(displayRole).append(": ").append(message).append("\n");
                        log.debug("Added transcript turn from conversation_data - role: {}, message: {}", displayRole, message);
                    }
                }
                detailedTranscript = conversationDataBuilder.toString().trim();
                log.debug("Extracted detailed transcript from 'conversation_data.transcript': {}", detailedTranscript);
            }
        }
        
        // If still no detailed transcript, try to fetch detailed conversation data
        if (detailedTranscript == null || detailedTranscript.isEmpty()) {
            log.info("No detailed transcript found in basic conversation data, fetching detailed conversation for: {}", conversationId);
            detailedConversation = fetchConversationDetails(conversationId);
            if (detailedConversation != null) {
                log.info("Successfully fetched detailed conversation data for: {}", conversationId);
                
                // Get transcript summary from detailed conversation if not already found
                if (transcriptSummary == null && detailedConversation.has("analysis") && detailedConversation.get("analysis").has("transcript_summary")) {
                    transcriptSummary = detailedConversation.get("analysis").get("transcript_summary").asText();
                    log.info("Found transcript summary in detailed conversation: {}", transcriptSummary);
                }
                
                // Try to extract detailed transcript from detailed conversation
                if (detailedConversation.has("transcript") && detailedConversation.get("transcript").isArray()) {
                    StringBuilder detailedBuilder = new StringBuilder();
                    JsonNode transcriptArray = detailedConversation.get("transcript");
                    log.info("Found transcript array in detailed conversation with {} elements", transcriptArray.size());
                    
                    for (JsonNode turn : transcriptArray) {
                        if (turn.has("message")) {
                            String message = turn.get("message").asText();
                            String role = turn.has("role") ? turn.get("role").asText() : "unknown";
                            // Map role to AI/User format
                            String displayRole = "agent".equalsIgnoreCase(role) ? "AI" : "User";
                            detailedBuilder.append(displayRole).append(": ").append(message).append("\n");
                            log.debug("Added transcript turn from detailed conversation - role: {}, message: {}", displayRole, message);
                        } else {
                            log.debug("Transcript turn missing 'message' field: {}", turn.toString());
                        }
                    }
                    detailedTranscript = detailedBuilder.toString().trim();
                    log.info("Extracted detailed transcript from detailed conversation ({} chars): {}", 
                            detailedTranscript.length(), detailedTranscript.substring(0, Math.min(100, detailedTranscript.length())) + "...");
                } else {
                    log.warn("No detailed transcript found in detailed conversation either. Available fields: {}", 
                             StreamSupport.stream(Spliterators.spliteratorUnknownSize(detailedConversation.fieldNames(), 0), false)
                                 .collect(Collectors.toList()));
                }
                
                // Extract timestamps from detailed conversation if not already found
                if (startedAt == null && detailedConversation.has("metadata")) {
                    JsonNode detailedMetadata = detailedConversation.get("metadata");
                    
                    // Extract start time from detailed conversation metadata
                    if (detailedMetadata.has("start_time_unix_secs")) {
                        long startTimeUnix = detailedMetadata.get("start_time_unix_secs").asLong();
                        startedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneOffset.UTC);
                        log.debug("Extracted startedAt from detailed conversation metadata: {} -> {}", startTimeUnix, startedAt);
                    }
                    
                    // Extract end time by adding duration to start time
                    if (startedAt != null && detailedMetadata.has("call_duration_secs")) {
                        long durationSecs = detailedMetadata.get("call_duration_secs").asLong();
                        endedAt = startedAt.plusSeconds(durationSecs);
                        log.debug("Calculated endedAt from detailed conversation: startedAt {} + {} seconds = {}", startedAt, durationSecs, endedAt);
                    }
                }
                
                // Phone number extraction will be handled later in the method
            } else {
                log.error("Failed to fetch detailed conversation data for: {}", conversationId);
            }
        }
        
        // Combine detailed transcript and summary
        String transcript = null;
        if (detailedTranscript != null && !detailedTranscript.isEmpty()) {
            if (transcriptSummary != null && !transcriptSummary.isEmpty()) {
                // Both detailed transcript and summary available
                transcript = detailedTranscript + "\n\nSummary: " + transcriptSummary;
                log.debug("Combined detailed transcript and summary: {} chars", transcript.length());
            } else {
                // Only detailed transcript available
                transcript = detailedTranscript;
                log.debug("Using only detailed transcript: {} chars", transcript.length());
            }
        } else if (transcriptSummary != null && !transcriptSummary.isEmpty()) {
            // Only summary available
            transcript = transcriptSummary;
            log.debug("Using only transcript summary: {} chars", transcript.length());
        }
        
        log.debug("Final combined transcript: {}", transcript);

        // Extract phone number from conversation data (both basic and detailed)
        // Priority order: detailed conversation first, then basic conversation
        String phoneNumber = null;
        
        // 1. Check detailed conversation first (if available)
        if (detailedConversation != null) {
            // metadata.phone_call.external_number
            if ((phoneNumber == null || phoneNumber.isEmpty()) && detailedConversation.has("metadata")) {
                JsonNode detailedMetadata = detailedConversation.get("metadata");
                if (detailedMetadata.has("phone_call") && detailedMetadata.get("phone_call").has("external_number")) {
                    phoneNumber = detailedMetadata.get("phone_call").get("external_number").asText(null);
                    log.info("Found phone number in detailed conversation metadata.phone_call.external_number: {}", phoneNumber);
                }
            }
            // conversation_initiation_client_data.dynamic_variables.system__caller_id
            if ((phoneNumber == null || phoneNumber.isEmpty()) && detailedConversation.has("conversation_initiation_client_data")) {
                JsonNode detailedDynVars = detailedConversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (detailedDynVars.has("system__caller_id")) {
                    phoneNumber = detailedDynVars.get("system__caller_id").asText(null);
                    log.info("Found phone number in detailed conversation system__caller_id: {}", phoneNumber);
                } else if (detailedDynVars.has("system__called_number")) {
                    phoneNumber = detailedDynVars.get("system__called_number").asText(null);
                    log.info("Found phone number in detailed conversation system__called_number: {}", phoneNumber);
                }
            }
        }
        
        // 2. Check basic conversation if phone number still not found
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // direct phone_number field
            if (conversation.has("phone_number")) {
                phoneNumber = conversation.get("phone_number").asText(null);
                log.debug("Found phone number in direct phone_number field: {}", phoneNumber);
            }
            // metadata.phone_call.external_number (new ElevenLabs format)
            if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("metadata")) {
                JsonNode metadata = conversation.get("metadata");
                if (metadata.has("phone_call") && metadata.get("phone_call").has("external_number")) {
                    phoneNumber = metadata.get("phone_call").get("external_number").asText(null);
                    log.debug("Found phone number in metadata.phone_call.external_number: {}", phoneNumber);
                }
            }
            // conversation_initiation_client_data.dynamic_variables.system__caller_id
            if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__caller_id")) {
                    phoneNumber = dynVars.get("system__caller_id").asText(null);
                    log.debug("Found phone number in system__caller_id: {}", phoneNumber);
                }
            }
            // conversation_initiation_client_data.dynamic_variables.system__called_number
            if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__called_number")) {
                    phoneNumber = dynVars.get("system__called_number").asText(null);
                    log.debug("Found phone number in system__called_number: {}", phoneNumber);
                }
            }
        }
        
        log.debug("Final extracted phoneNumber: {}", phoneNumber);

        // Extract audio URL from ElevenLabs API
        String audioUrl = null;
        
        // Check if audio is available - try multiple indicators
        boolean hasAudio = false;
        if (conversation.has("has_audio")) {
            hasAudio = conversation.get("has_audio").asBoolean();
        } else if (conversation.has("has_user_audio")) {
            hasAudio = conversation.get("has_user_audio").asBoolean();
        } else if (conversation.has("has_response_audio")) {
            hasAudio = conversation.get("has_response_audio").asBoolean();
        } else {
            // If no explicit audio flag, try to get audio anyway (some conversations might have audio without the flag)
            hasAudio = true;
            log.debug("No explicit audio flag found for conversation {}, will attempt to get audio", conversationId);
        }
        
        if (hasAudio) {
            // Build the audio URL using the ElevenLabs API endpoint
            String elevenLabsAudioUrl = buildElevenLabsAudioUrl(conversationId);
            log.debug("Generated ElevenLabs audio URL for conversation {}: {}", conversationId, elevenLabsAudioUrl);
            
            // Download and store the audio file locally with proper authentication
            try {
                String storedPath = downloadAndStoreElevenLabsAudio(elevenLabsAudioUrl, conversationId);
                String publicUrl = audioStorageService.getPublicUrl(storedPath);
                
                if (publicUrl != null && !publicUrl.isEmpty()) {
                    audioUrl = publicUrl;
                    log.info("Stored ElevenLabs audio locally for conversation {}: {} -> {}", 
                            conversationId, elevenLabsAudioUrl, audioUrl);
                } else {
                    // Fallback to original URL if storage fails
                    audioUrl = elevenLabsAudioUrl;
                    log.warn("Failed to store audio locally for conversation {}, using original URL: {}", 
                            conversationId, elevenLabsAudioUrl);
                }
            } catch (Exception e) {
                log.error("Error storing audio for conversation {}: {}", conversationId, e.getMessage());
                // Fallback to original URL
                audioUrl = elevenLabsAudioUrl;
            }
        } else {
            log.debug("No audio available for conversation: {}", conversationId);
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
        log.debug("Extracted durationMinutes: {}", durationMinutes);

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
        dto.setAudioUrl(audioUrl);
        dto.setDurationMinutes(durationMinutes);
        dto.setRawPayload(rawData);
        
        // Debug logging for DTO creation
        log.debug("Created VoiceLogCreateDTO for conversation {}: transcript={}, durationMinutes={}, phoneNumber={}, audioUrl={}", 
                 conversationId, 
                 transcript != null ? "present (" + transcript.length() + " chars)" : "null",
                 durationMinutes,
                 phoneNumber,
                 audioUrl != null ? "present" : "null");
        
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
     * Build ElevenLabs conversation details URL (for getting transcript)
     */
    private String buildElevenLabsConversationDetailsUrl(String conversationId) {
        return UriComponentsBuilder
                .fromHttpUrl(elevenLabsConfig.getApiUrl())
                .path("/v1/convai/conversations/{conversationId}")
                .buildAndExpand(conversationId)
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
     * Download and store ElevenLabs audio with proper authentication
     */
    private String downloadAndStoreElevenLabsAudio(String audioUrl, String conversationId) throws IOException {
        log.debug("Downloading ElevenLabs audio with authentication for conversation: {}", conversationId);
        
        // Create headers with ElevenLabs API key
        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", elevenLabsConfig.getApiKey());
        
        // Make authenticated request to ElevenLabs audio endpoint
        ResponseEntity<byte[]> response = restTemplate.exchange(
            audioUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.debug("Successfully downloaded audio for conversation: {} ({} bytes)", conversationId, response.getBody().length);
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "call_" + conversationId + "_" + timestamp + ".mp3";
            
            // Save to local storage
            String storedPath = saveAudioToLocalStorage(response.getBody(), fileName);
            log.info("Stored ElevenLabs audio locally: {} -> {}", audioUrl, storedPath);
            
            return storedPath;
        } else {
            log.warn("Failed to download audio for conversation: {}, status: {}", conversationId, response.getStatusCode());
            throw new IOException("Failed to download audio: HTTP " + response.getStatusCode());
        }
    }
    
    /**
     * Save audio bytes to local storage
     */
    private String saveAudioToLocalStorage(byte[] audioData, String fileName) throws IOException {
        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get("uploads/audio");
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
            log.info("Created audio storage directory: {}", storageDir);
        }
        
        // Save the audio file
        Path targetPath = storageDir.resolve(fileName);
        Files.write(targetPath, audioData);
        
        log.debug("Saved audio file to: {}", targetPath);
        return targetPath.toString();
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