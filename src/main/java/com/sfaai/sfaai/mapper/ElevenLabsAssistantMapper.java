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
        
        // Map client and agent relationships
        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
        }
        if (entity.getAgent() != null) {
            dto.setAgentId(entity.getAgent().getId());
        }
        
        dto.setFirstMessage(entity.getFirstMessage());
        dto.setLanguage(entity.getLanguage());
        dto.setVoiceProvider(entity.getVoiceProvider());
        dto.setVoiceId(entity.getVoiceId());
        dto.setModelProvider(entity.getModelProvider());
        dto.setModelName(entity.getModelName());
        dto.setTranscriberProvider(entity.getTranscriberProvider());
        dto.setTranscriberModel(entity.getTranscriberModel());
        dto.setTranscriberLanguage(entity.getTranscriberLanguage());
        dto.setPrompt(entity.getPrompt());
        dto.setKnowledgeBaseIds(entity.getKnowledgeBaseIds());
        dto.setConversationConfig(entity.getConversationConfig());
        dto.setSyncStatus(entity.getSyncStatus());
        dto.setRawData(entity.getRawData());
        return dto;
    }

    public ElevenLabsAssistant toEntity(ElevenLabsAssistantDTO dto) {
        if (dto == null) {
            return null;
        }

        ElevenLabsAssistant entity = new ElevenLabsAssistant();
        entity.setAssistantId(dto.getAssistantId());
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
        entity.setRawData(dto.getRawData());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
