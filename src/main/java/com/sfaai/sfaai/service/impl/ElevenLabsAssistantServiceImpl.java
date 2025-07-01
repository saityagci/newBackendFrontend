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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsAssistantServiceImpl implements ElevenLabsAssistantService {

    private final ElevenLabsConfig elevenLabsConfig;
    private final RestTemplate restTemplate;
    private final ElevenLabsAssistantRepository assistantRepository;
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
        for (ElevenLabsAssistantDTO assistantDto : apiAssistants.getAssistants()) {
            try {
                String assistantId = assistantDto.getAssistantId();
                Optional<ElevenLabsAssistant> existingAssistant = assistantRepository.findById(assistantId);

                ElevenLabsAssistant assistant;
                if (existingAssistant.isPresent()) {
                    assistant = existingAssistant.get();
                    // Update existing fields
                    assistant.setName(assistantDto.getName());
                    assistant.setDescription(assistantDto.getDescription());
                    assistant.setVoiceId(assistantDto.getVoiceId());
                    assistant.setVoiceName(assistantDto.getVoiceName());
                    assistant.setModelId(assistantDto.getModelId());
                } else {
                    // Create new assistant
                    assistant = assistantMapper.toEntity(assistantDto);
                }

                // Set raw data for reference
                assistant.setRawData(objectMapper.writeValueAsString(assistantDto));
                assistant.setLastSyncedAt(LocalDateTime.now());

                assistantRepository.save(assistant);
                count++;
            } catch (Exception e) {
                log.error("Error syncing ElevenLabs assistant: {}", assistantDto.getAssistantId(), e);
            }
        }

        log.info("Synchronized {} ElevenLabs assistants", count);
        return count;
    }
}
