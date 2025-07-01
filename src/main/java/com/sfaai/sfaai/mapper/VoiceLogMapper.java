package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VoiceLogMapper implements EntityMapper<VoiceLogDTO, VoiceLog> {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Override
    public VoiceLogDTO toDto(VoiceLog entity) {
        if (entity == null) {
            return null;
        }

        return VoiceLogDTO.builder()
                .id(entity.getId())
                .agentId(entity.getAgent().getId())
                .clientId(entity.getClient().getId())
                .provider(String.valueOf(entity.getProvider()))
                .externalCallId(entity.getExternalCallId())
                .externalAgentId(entity.getExternalAgentId())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .audioUrl(entity.getAudioUrl())
                .transcript(entity.getTranscript())
                .rawPayload(entity.getRawPayload())
                .conversationData(entity.getConversationData())
                .createdAt(entity.getCreatedAt())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }

    @Override
    public VoiceLog toEntity(VoiceLogDTO dto) {
        if (dto == null) {
            return null;
        }

        VoiceLog voiceLog = VoiceLog.builder()
                .externalCallId(dto.getExternalCallId())
                .provider(VoiceLog.Provider.valueOf(dto.getProvider().toUpperCase()))
                .externalAgentId(dto.getExternalAgentId())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .audioUrl(dto.getAudioUrl())
                .transcript(dto.getTranscript())
                .rawPayload(dto.getRawPayload())
                .conversationData(dto.getConversationData())
                .status(VoiceLog.Status.INITIATED)
                .durationMinutes(dto.getDurationMinutes())
                .build();

        if (dto.getId() != null) {
            voiceLog.setId(dto.getId());
        }

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + dto.getClientId()));
            voiceLog.setClient(client);
        }

        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + dto.getAgentId()));
            voiceLog.setAgent(agent);
        }

        return voiceLog;
    }

    public VoiceLog createEntityFromDto(VoiceLogCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        // Use specified status or default to INITIATED
        VoiceLog.Status status = dto.getStatus() != null ? dto.getStatus() : VoiceLog.Status.INITIATED;

        // Calculate duration minutes if not provided but we have start and end times
        Double durationMinutes = dto.getDurationMinutes();
        if (durationMinutes == null && dto.getStartedAt() != null && dto.getEndedAt() != null) {
            long seconds = java.time.Duration.between(dto.getStartedAt(), dto.getEndedAt()).getSeconds();
            durationMinutes = seconds / 60.00;
        }

                        // Safe conversion of provider string to enum with proper error handling
                        VoiceLog.Provider provider;
                        try {
                            provider = VoiceLog.Provider.valueOf(dto.getProvider().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Invalid provider value: " + dto.getProvider() + 
                ". Allowed values are: " + java.util.Arrays.toString(VoiceLog.Provider.values()));
                        }

                        VoiceLog voiceLog = VoiceLog.builder()
                .externalCallId(dto.getExternalCallId())
                .provider(provider)
                .externalAgentId(dto.getExternalAgentId())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .audioUrl(dto.getAudioUrl())
                .transcript(dto.getTranscript())
                .rawPayload(dto.getRawPayload())
                .conversationData(dto.getConversationData())
                .status(status)
                .phoneNumber(dto.getPhoneNumber())
                .durationMinutes(dto.getDurationMinutes())
                .build();

                        // Log the durationMinutes value to verify it's being set
                        org.slf4j.LoggerFactory.getLogger(VoiceLogMapper.class)
                .debug("Setting durationMinutes in VoiceLog entity: {}", durationMinutes);

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + dto.getClientId()));
            voiceLog.setClient(client);
        }

        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + dto.getAgentId()));
            voiceLog.setAgent(agent);
        }

        return voiceLog;
    }

    @Override
    public List<VoiceLogDTO> toDtoList(List<VoiceLog> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoiceLog> toEntityList(List<VoiceLogDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
