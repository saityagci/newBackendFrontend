package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ElevenLabsAssistantMapper {

    public ElevenLabsAssistantDTO toDto(ElevenLabsAssistant entity) {
        if (entity == null) {
            return null;
        }

        ElevenLabsAssistantDTO dto = new ElevenLabsAssistantDTO();
        dto.setAssistantId(entity.getAssistantId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setVoiceId(entity.getVoiceId());
        dto.setVoiceName(entity.getVoiceName());
        dto.setModelId(entity.getModelId());
        return dto;
    }

    public ElevenLabsAssistant toEntity(ElevenLabsAssistantDTO dto) {
        if (dto == null) {
            return null;
        }

        ElevenLabsAssistant entity = new ElevenLabsAssistant();
        entity.setAssistantId(dto.getAssistantId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setVoiceId(dto.getVoiceId());
        entity.setVoiceName(dto.getVoiceName());
        entity.setModelId(dto.getModelId());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
