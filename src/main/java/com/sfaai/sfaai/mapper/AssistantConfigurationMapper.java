package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.AssistantConfigurationDTO;
import com.sfaai.sfaai.entity.AssistantConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssistantConfigurationMapper {

    public AssistantConfigurationDTO toDto(AssistantConfiguration assistantConfiguration) {
        if (assistantConfiguration == null) {
            return null;
        }

        return AssistantConfigurationDTO.builder()
                .id(assistantConfiguration.getId())
                .assistantId(assistantConfiguration.getAssistantId())
                .subject(assistantConfiguration.getSubject())
                .description(assistantConfiguration.getDescription())
                .status(assistantConfiguration.getStatus())
                .clientId(assistantConfiguration.getClientId())
                .clientEmail(assistantConfiguration.getClientEmail())
                .clientName(assistantConfiguration.getClientName())
                .createdAt(assistantConfiguration.getCreatedAt())
                .updatedAt(assistantConfiguration.getUpdatedAt())
                .build();
    }

    public AssistantConfiguration toEntity(AssistantConfigurationDTO assistantConfigurationDTO) {
        if (assistantConfigurationDTO == null) {
            return null;
        }

        return AssistantConfiguration.builder()
                .assistantId(assistantConfigurationDTO.getAssistantId())
                .subject(assistantConfigurationDTO.getSubject())
                .description(assistantConfigurationDTO.getDescription())
                .status(assistantConfigurationDTO.getStatus() != null ? assistantConfigurationDTO.getStatus() : AssistantConfiguration.Status.PENDING)
                .clientId(assistantConfigurationDTO.getClientId())
                .clientEmail(assistantConfigurationDTO.getClientEmail())
                .clientName(assistantConfigurationDTO.getClientName())
                .build();
    }

    public List<AssistantConfigurationDTO> toDtoList(List<AssistantConfiguration> assistantConfigurations) {
        if (assistantConfigurations == null) {
            return null;
        }

        return assistantConfigurations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 