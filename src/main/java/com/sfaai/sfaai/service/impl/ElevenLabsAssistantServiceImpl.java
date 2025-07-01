package com.sfaai.sfaai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.config.ElevenLabsConfig;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;
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

    @Override
    public int syncAllAssistants() {
        log.info("Starting synchronization of ElevenLabs assistants");
        ElevenLabsListAssistantsResponse apiAssistants = getAllAssistantsFromApi();

        if (apiAssistants == null || apiAssistants.getAssistants() == null) {
            log.warn("No assistants returned from ElevenLabs API");
            return 0;
        }

        int count = 0;
        List<ElevenLabsAssistant> toSave = new java.util.ArrayList<>();
        for (ElevenLabsAssistantDTO assistantDto : apiAssistants.getAssistants()) {
            try {
                String assistantId = assistantDto.getAssistantId();
                ElevenLabsAssistantDTO fullDetails = fetchAssistantDetailsFromApi(assistantId);
                if (fullDetails == null) {
                    log.warn("Could not fetch details for assistant {}", assistantId);
                    continue;
                }
                Optional<ElevenLabsAssistant> existingAssistantOpt = assistantRepository.findById(assistantId);
                ElevenLabsAssistant assistantEntity;
                boolean changed = false;
                if (existingAssistantOpt.isPresent()) {
                    assistantEntity = existingAssistantOpt.get();
                    // Only update if changed
                    String newRaw = objectMapper.writeValueAsString(fullDetails);
                    if (!newRaw.equals(assistantEntity.getRawData())) {
                        assistantEntity.setName(fullDetails.getName());
                        assistantEntity.setDescription(fullDetails.getDescription());
                        assistantEntity.setVoiceId(fullDetails.getVoiceId());
                        assistantEntity.setVoiceName(fullDetails.getVoiceName());
                        assistantEntity.setModelId(fullDetails.getModelId());
                        assistantEntity.setRawData(newRaw);
                        assistantEntity.setLastSyncedAt(LocalDateTime.now());
                        changed = true;
                    }
                } else {
                    assistantEntity = assistantMapper.toEntity(fullDetails);
                    assistantEntity.setRawData(objectMapper.writeValueAsString(fullDetails));
                    assistantEntity.setLastSyncedAt(LocalDateTime.now());
                    changed = true;
                }
                if (changed) {
                    toSave.add(assistantEntity);
                    count++;
                }
            } catch (Exception e) {
                log.error("Error syncing ElevenLabs assistant: {}", assistantDto.getAssistantId(), e);
            }
        }
        if (!toSave.isEmpty()) {
            assistantRepository.saveAll(toSave);
        }
        log.info("Synchronized {} ElevenLabs assistants (inserted/updated)", count);
        return count;
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
}
