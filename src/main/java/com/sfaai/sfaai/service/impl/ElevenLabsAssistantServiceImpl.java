package com.sfaai.sfaai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.config.ElevenLabsConfig;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDetailResponse;
import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import com.sfaai.sfaai.mapper.ElevenLabsAssistantMapper;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.service.ElevenLabsAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsAssistantServiceImpl implements ElevenLabsAssistantService {

    private final ElevenLabsConfig elevenLabsConfig;
    private final RestTemplate restTemplate;
    private final ElevenLabsAssistantRepository assistantRepository;
    @Qualifier("elevenLabsAssistantMapper")
    private final ElevenLabsAssistantMapper assistantMapper;
    private final ObjectMapper objectMapper;
    

    @Override
    public ElevenLabsListAssistantsResponse getAllAssistantsFromApi() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", elevenLabsConfig.getApiKey());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(elevenLabsConfig.getApiUrl())
                .path("/v1/convai/agents")
                .build()
                .toUriString();

        ResponseEntity<ElevenLabsListAssistantsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ElevenLabsListAssistantsResponse.class
        );

        return response.getBody();
    }

    @Override
    public List<ElevenLabsAssistantDTO> getAllAssistants() {
        List<ElevenLabsAssistant> assistants = assistantRepository.findAll();
        return assistants.stream()
                .map(assistantMapper::toDto)
                .toList();
    }

    @Override
    public ElevenLabsAssistantDTO getAssistant(String assistantId) {
        Optional<ElevenLabsAssistant> assistantOpt = assistantRepository.findById(assistantId);
        return assistantOpt.map(assistantMapper::toDto).orElse(null);
    }

    public static class SyncSummary {
        public int fetched;
        public int updated;
        public int skipped;
        public int errors;
        public long durationMs;
        public java.util.List<String> updatedIds = new java.util.ArrayList<>();
        public java.util.List<String> skippedIds = new java.util.ArrayList<>();
        public java.util.List<String> errorIds = new java.util.ArrayList<>();
    }

    @Override
    public int syncAllAssistants() {
        SyncSummary summary = syncAllAssistantsWithSummary();
        return summary.updated;
    }

    public SyncSummary syncAllAssistantsWithSummary() {
        long start = System.currentTimeMillis();
        SyncSummary summary = new SyncSummary();
        ElevenLabsListAssistantsResponse apiAssistants = null;
        try {
            apiAssistants = getAllAssistantsFromApi();
        } catch (Exception e) {
            log.error("Error fetching assistants from ElevenLabs API", e);
            summary.errors++;
            summary.durationMs = System.currentTimeMillis() - start;
            return summary;
        }
        if (apiAssistants == null || apiAssistants.getAssistants() == null) {
            log.warn("No assistants returned from ElevenLabs API");
            summary.durationMs = System.currentTimeMillis() - start;
            return summary;
        }
        summary.fetched = apiAssistants.getAssistants().size();
        int count = 0;
        for (ElevenLabsAssistantDTO assistantDto : apiAssistants.getAssistants()) {
            try {
                String assistantId = assistantDto.getAssistantId();
                ElevenLabsAssistantDetailResponse fullDetails = getAssistantDetailsFromApi(assistantId);
                if (fullDetails == null) {
                    log.warn("Could not fetch details for assistant {}", assistantId);
                    summary.errorIds.add(assistantId);
                    summary.errors++;
                    continue;
                }
                ElevenLabsAssistantDTO detailDto = mapDetailResponseToDto(fullDetails);
                Optional<ElevenLabsAssistant> existingAssistantOpt = assistantRepository.findById(assistantId);
                ElevenLabsAssistant assistantEntity;
                boolean changed = false;
                String newRaw = objectMapper.writeValueAsString(fullDetails);
                if (existingAssistantOpt.isPresent()) {
                    assistantEntity = existingAssistantOpt.get();
                    if (!newRaw.equals(assistantEntity.getRawData())) {
                        updateEntityFromDto(assistantEntity, detailDto);
                        assistantEntity.setRawData(newRaw);
                        assistantEntity.setLastSyncedAt(LocalDateTime.now());
                        changed = true;
                        log.info("Updating assistant {} (raw JSON changed)", assistantId);
                        summary.updatedIds.add(assistantId);
                    } else {
                        log.info("Assistant {} up-to-date (raw JSON unchanged)", assistantId);
                        summary.skippedIds.add(assistantId);
                        summary.skipped++;
                    }
                } else {
                    assistantEntity = assistantMapper.toEntity(detailDto);
                    assistantEntity.setRawData(newRaw);
                    assistantEntity.setLastSyncedAt(LocalDateTime.now());
                    changed = true;
                    log.info("Inserting new assistant {}", assistantId);
                    summary.updatedIds.add(assistantId);
                }
                if (changed) {
                    assistantRepository.save(assistantEntity);
                    count++;
                    summary.updated++;
                }
            } catch (Exception e) {
                log.error("Error syncing ElevenLabs assistant: {}", assistantDto.getAssistantId(), e);
                summary.errorIds.add(assistantDto.getAssistantId());
                summary.errors++;
            }
        }
        summary.durationMs = System.currentTimeMillis() - start;
        if (summary.updated == 0) {
            log.info("No assistants updated; all records up-to-date");
        } else {
            log.info("{} assistants saved/updated", summary.updated);
        }
        log.info("{} assistants fetched from API", summary.fetched);
        return summary;
    }

    /**
     * Map ElevenLabsAssistantDetailResponse to ElevenLabsAssistantDTO
     */
    private ElevenLabsAssistantDTO mapDetailResponseToDto(ElevenLabsAssistantDetailResponse detail) {
        ElevenLabsAssistantDTO dto = new ElevenLabsAssistantDTO();
        dto.setAssistantId(detail.getAssistantId());
        dto.setName(detail.getName());
        dto.setDescription(detail.getDescription());
        dto.setVoiceId(detail.getVoiceId());
        dto.setVoiceName(detail.getVoiceName());
        dto.setModelId(detail.getModelId());

        // --- Conversation Config ---
        if (detail.getConversationConfig() != null) {
            var cc = detail.getConversationConfig();
            String firstMessage = null;
            if (cc.getFirstMessage() != null) {
                firstMessage = cc.getFirstMessage();
            } else if (cc.getAgent() != null && cc.getAgent().getFirstMessage() != null) {
                firstMessage = cc.getAgent().getFirstMessage();
            }
            if (firstMessage != null) {
                log.debug("Setting firstMessage for assistant {}: {}", detail.getAssistantId(), firstMessage);
                dto.setFirstMessage(firstMessage);
            } else {
                log.debug("No firstMessage found for assistant {} in conversation_config", detail.getAssistantId());
            }
            if (cc.getLanguage() != null) dto.setLanguage(cc.getLanguage());
            if (cc.getPrompt() != null && cc.getPrompt().getPrompt() != null) dto.setPrompt(cc.getPrompt().getPrompt());
            // If conversation_config is needed as JSON, store it
            try {
                dto.setConversationConfig(objectMapper.writeValueAsString(cc));
            } catch (Exception e) {
                log.warn("Could not serialize conversation_config for assistant {}: {}", detail.getAssistantId(), e.getMessage());
            }
        }

        // --- Agent ---
        if (detail.getAgent() != null) {
            if (detail.getAgent().getName() != null) dto.setName(detail.getAgent().getName());
            // Add more agent fields as needed
        }

        // --- TTS ---
        if (detail.getTts() != null) {
            if (detail.getTts().getVoiceId() != null) dto.setVoiceId(detail.getTts().getVoiceId());
            // Add more TTS fields as needed
        }

        // --- Tools ---
        Object toolsObj = detail.getTools();
        if (toolsObj != null) {
            if (toolsObj instanceof java.util.List || toolsObj instanceof java.util.Map) {
                // Not mapped to entity, but log for awareness
                log.debug("Assistant {} has tools field of type {}", detail.getAssistantId(), toolsObj.getClass().getSimpleName());
            } else {
                log.warn("Assistant {}: tools field is of unexpected type {}", detail.getAssistantId(), toolsObj.getClass().getName());
            }
        }

        // --- Details Map (for extra fields) ---
        Map<String, Object> details = detail.getDetails();
        if (details != null) {
            // voiceProvider
            Object voiceProvider = details.get("voice_provider");
            if (voiceProvider instanceof String) dto.setVoiceProvider((String) voiceProvider);
            else if (voiceProvider != null) log.warn("voiceProvider is not a String for assistant {}", detail.getAssistantId());

            // modelProvider
            Object modelProvider = details.get("model_provider");
            if (modelProvider instanceof String) dto.setModelProvider((String) modelProvider);
            else if (modelProvider != null) log.warn("modelProvider is not a String for assistant {}", detail.getAssistantId());

            // modelName
            Object modelName = details.get("model_name");
            if (modelName instanceof String) dto.setModelName((String) modelName);
            else if (modelName != null) log.warn("modelName is not a String for assistant {}", detail.getAssistantId());

            // transcriberProvider
            Object transcriberProvider = details.get("transcriber_provider");
            if (transcriberProvider instanceof String) dto.setTranscriberProvider((String) transcriberProvider);
            else if (transcriberProvider != null) log.warn("transcriberProvider is not a String for assistant {}", detail.getAssistantId());

            // transcriberModel
            Object transcriberModel = details.get("transcriber_model");
            if (transcriberModel instanceof String) dto.setTranscriberModel((String) transcriberModel);
            else if (transcriberModel != null) log.warn("transcriberModel is not a String for assistant {}", detail.getAssistantId());

            // transcriberLanguage
            Object transcriberLanguage = details.get("transcriber_language");
            if (transcriberLanguage instanceof String) dto.setTranscriberLanguage((String) transcriberLanguage);
            else if (transcriberLanguage != null) log.warn("transcriberLanguage is not a String for assistant {}", detail.getAssistantId());

            // knowledgeBaseIds
            Object kbIds = details.get("knowledge_base_ids");
            if (kbIds instanceof String) dto.setKnowledgeBaseIds((String) kbIds);
            else if (kbIds instanceof java.util.List) dto.setKnowledgeBaseIds(String.join(",", ((java.util.List<?>) kbIds).stream().map(Object::toString).toList()));
            else if (kbIds != null) log.warn("knowledgeBaseIds is not a String or List for assistant {}", detail.getAssistantId());

            // syncStatus
            Object syncStatus = details.get("sync_status");
            if (syncStatus instanceof String) dto.setSyncStatus((String) syncStatus);
            else if (syncStatus != null) log.warn("syncStatus is not a String for assistant {}", detail.getAssistantId());
        }

        // --- Deeply Nested Fields (robust extraction) ---
        // Example: conversation_config.agent.first_message
        try {
            if (detail.getConversationConfig() != null && detail.getConversationConfig().getFirstMessage() != null) {
                dto.setFirstMessage(detail.getConversationConfig().getFirstMessage());
            }
            // If there are more deeply nested fields, extract and log them here
        } catch (Exception e) {
            log.warn("Error extracting deeply nested fields for assistant {}: {}", detail.getAssistantId(), e.getMessage());
        }

        dto.setRawData(null); // Will be set separately
        return dto;
    }

    /**
     * Update an existing entity from DTO (without replacing the entity)
     */
    private void updateEntityFromDto(ElevenLabsAssistant entity, ElevenLabsAssistantDTO dto) {
        entity.setName(dto.getName());
        entity.setFirstMessage(dto.getFirstMessage());
        entity.setLanguage(dto.getLanguage());
        entity.setVoiceProvider(dto.getVoiceProvider());
        entity.setVoiceId(dto.getVoiceId());
        entity.setModelProvider(dto.getModelProvider());
        entity.setModelName(dto.getModelName());
        entity.setTranscriberProvider(dto.getTranscriberProvider());
        entity.setTranscriberModel(dto.getTranscriberModel());
        entity.setTranscriberLanguage(dto.getTranscriberLanguage());
        entity.setPrompt(dto.getPrompt());
        entity.setKnowledgeBaseIds(dto.getKnowledgeBaseIds());
        entity.setConversationConfig(dto.getConversationConfig());
        entity.setSyncStatus(dto.getSyncStatus());
    }

    /**
     * Fetch full details for a single ElevenLabs assistant by ID
     */
    private ElevenLabsAssistantDTO fetchAssistantDetailsFromApi(String assistantId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("xi-api-key", elevenLabsConfig.getApiKey());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = UriComponentsBuilder
                    .fromHttpUrl(elevenLabsConfig.getApiUrl())
                    .path("/v1/convai/agents/")
                    .path(assistantId)
                    .build()
                    .toUriString();
            ResponseEntity<ElevenLabsAssistantDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ElevenLabsAssistantDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch full details for ElevenLabs assistant {}: {}", assistantId, e.getMessage());
            return null;
        }
    }

    @Override
    public com.sfaai.sfaai.dto.ElevenLabsAssistantDetailResponse getAssistantDetailsFromApi(String assistantId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("xi-api-key", elevenLabsConfig.getApiKey());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = UriComponentsBuilder
                    .fromHttpUrl(elevenLabsConfig.getApiUrl())
                    .path("/v1/convai/agents/")
                    .path(assistantId)
                    .build()
                    .toUriString();
            ResponseEntity<com.sfaai.sfaai.dto.ElevenLabsAssistantDetailResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.sfaai.sfaai.dto.ElevenLabsAssistantDetailResponse.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Failed to fetch assistant details for {}: status {}", assistantId, response.getStatusCode());
                throw new com.sfaai.sfaai.exception.ExternalApiException("Assistant not found or error from ElevenLabs API", "ElevenLabs", "NOT_FOUND");
            }
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("Assistant not found in ElevenLabs API: {}", assistantId);
            throw new com.sfaai.sfaai.exception.ExternalApiException("Assistant not found in ElevenLabs API", "ElevenLabs", "NOT_FOUND", e);
        } catch (Exception e) {
            log.error("Error fetching assistant details from ElevenLabs API for {}: {}", assistantId, e.getMessage());
            throw new com.sfaai.sfaai.exception.ExternalApiException("Failed to fetch assistant details: " + e.getMessage(), "ElevenLabs", "API_ERROR", e);
        }
    }
}
