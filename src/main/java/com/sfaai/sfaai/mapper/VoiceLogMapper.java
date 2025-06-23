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
                .provider(entity.getProvider())
                .externalCallId(entity.getExternalCallId())
                .externalAgentId(entity.getExternalAgentId())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .audioUrl(entity.getAudioUrl())
                .transcript(entity.getTranscript())
                .rawPayload(entity.getRawPayload())
                .conversationData(entity.getConversationData())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public VoiceLog toEntity(VoiceLogDTO dto) {
        if (dto == null) {
            return null;
        }

        VoiceLog voiceLog = VoiceLog.builder()
                .externalCallId(dto.getExternalCallId())
                .provider(dto.getProvider())
                .externalAgentId(dto.getExternalAgentId())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .audioUrl(dto.getAudioUrl())
                .transcript(dto.getTranscript())
                .rawPayload(dto.getRawPayload())
                .conversationData(dto.getConversationData())
                .status(VoiceLog.Status.INITIATED) // Default status
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

        VoiceLog voiceLog = VoiceLog.builder()
                .externalCallId(dto.getExternalCallId())
                .provider(dto.getProvider())
                .externalAgentId(dto.getExternalAgentId())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .audioUrl(dto.getAudioUrl())
                .transcript(dto.getTranscript())
                .rawPayload(dto.getRawPayload())
                .conversationData(dto.getConversationData())
                .status(VoiceLog.Status.INITIATED) // Default status
                .build();

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
