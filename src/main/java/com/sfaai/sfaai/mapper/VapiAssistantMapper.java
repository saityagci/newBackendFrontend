package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.entity.VapiAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between VapiAssistant entity and DTO
 */
@Component
@Slf4j
public class VapiAssistantMapper implements EntityMapper<VapiAssistantDTO, VapiAssistant> {

    private final com.sfaai.sfaai.util.FirstMessageFallbackAdapter firstMessageFallbackAdapter;

    public VapiAssistantMapper(com.sfaai.sfaai.util.FirstMessageFallbackAdapter firstMessageFallbackAdapter) {
        this.firstMessageFallbackAdapter = firstMessageFallbackAdapter;
    }

    @Override
    public VapiAssistantDTO toDto(VapiAssistant entity) {
        if (entity == null) {
            return null;
        }

        VapiAssistantDTO dto = new VapiAssistantDTO();
        dto.setAssistantId(entity.getAssistantId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());

        // Map client relationship
        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
        }

        // Debug firstMessage mapping
        String dbFirstMessage = entity.getFirstMessage();
        log.debug("VapiAssistantMapper.toDto: firstMessage from database: '{}'", dbFirstMessage);
        dto.setFirstMessage(dbFirstMessage);
        log.debug("VapiAssistantMapper.toDto: firstMessage set in DTO: '{}'", dto.getFirstMessage());

        // Apply fallback if firstMessage is still null
        if (dto.getFirstMessage() == null) {
            log.debug("VapiAssistantMapper.toDto: firstMessage is null, applying fallback");
            firstMessageFallbackAdapter.applyFallbackMessage(dto);
            log.debug("VapiAssistantMapper.toDto: fallback firstMessage: '{}'", dto.getFirstMessage());
        }

        // Map voice info if available
        if (entity.getVoiceProvider() != null || entity.getVoiceId() != null) {
            VapiAssistantDTO.VoiceInfo voiceInfo = new VapiAssistantDTO.VoiceInfo();
            voiceInfo.setProvider(entity.getVoiceProvider());
            voiceInfo.setVoiceId(entity.getVoiceId());
            dto.setVoice(voiceInfo);
        }

        // Map model info if available
        if (entity.getModelProvider() != null || entity.getModelName() != null) {
            VapiAssistantDTO.ModelInfo modelInfo = new VapiAssistantDTO.ModelInfo();
            modelInfo.setProvider(entity.getModelProvider());
            modelInfo.setModel(entity.getModelName());
            dto.setModel(modelInfo);
        }

        // Map transcriber info if available
        if (entity.getTranscriberProvider() != null || entity.getTranscriberModel() != null || entity.getTranscriberLanguage() != null) {
            VapiAssistantDTO.TranscriberInfo transcriberInfo = new VapiAssistantDTO.TranscriberInfo();
            transcriberInfo.setProvider(entity.getTranscriberProvider());
            transcriberInfo.setModel(entity.getTranscriberModel());
            transcriberInfo.setLanguage(entity.getTranscriberLanguage());
            dto.setTranscriber(transcriberInfo);
        }

        return dto;
    }

    @Override
    public VapiAssistant toEntity(VapiAssistantDTO dto) {
        if (dto == null) {
            log.warn("VapiAssistantMapper: Input DTO is null");
            return null;
        }

        log.debug("VapiAssistantMapper: Mapping DTO to entity - ID: {}, Name: {}", dto.getAssistantId(), dto.getName());

        try {
            VapiAssistant entity = new VapiAssistant();
            entity.setAssistantId(dto.getAssistantId());
            entity.setName(dto.getName() != null ? dto.getName() : "Unnamed Assistant"); // Prevent null name
            entity.setStatus(dto.getStatus());

                // Debug firstMessage field
                String firstMessage = dto.getFirstMessage();
                log.debug("FirstMessage from DTO: {}", firstMessage);
                entity.setFirstMessage(firstMessage);

        // Map voice info if available
        if (dto.getVoice() != null) {
            log.debug("Voice info from DTO: {}", dto.getVoice());
            entity.setVoiceProvider(dto.getVoice().getProvider());
            entity.setVoiceId(dto.getVoice().getVoiceId());
            log.debug("Set voice data - Provider: {}, ID: {}", entity.getVoiceProvider(), entity.getVoiceId());
        } else {
            log.debug("Voice info is null in DTO");
        }

        // Set model info if available
        if (dto.getModel() != null) {
            entity.setModelProvider(dto.getModel().getProvider());
            entity.setModelName(dto.getModel().getModel());
        }

        // Set transcriber info if available
        if (dto.getTranscriber() != null) {
            entity.setTranscriberProvider(dto.getTranscriber().getProvider());
            entity.setTranscriberModel(dto.getTranscriber().getModel());
            entity.setTranscriberLanguage(dto.getTranscriber().getLanguage());
        }

        // Set sync metadata
        entity.setLastSyncedAt(LocalDateTime.now());
        entity.setSyncStatus("SUCCESS");

        log.debug("VapiAssistantMapper: Successfully mapped entity - ID: {}, Name: {}", entity.getAssistantId(), entity.getName());
        return entity;
        } catch (Exception e) {
            log.error("VapiAssistantMapper: Error mapping entity: {}", e.getMessage(), e);
            throw e; // Re-throw to allow caller to handle it
        }
    }

    @Override
    public List<VapiAssistantDTO> toDtoList(List<VapiAssistant> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VapiAssistant> toEntityList(List<VapiAssistantDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
