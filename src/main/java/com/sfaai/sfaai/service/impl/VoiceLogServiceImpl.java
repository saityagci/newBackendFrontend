package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceLogServiceImpl implements VoiceLogService {

    private final VoiceLogRepository voiceLogRepository;
    private final AgentRepository agentRepository;
    private final ClientRepository clientRepository;

    @Override
    public VoiceLogDTO save(VoiceLogDTO dto) {
        VoiceLog entity = new VoiceLog();
        entity.setTranscript(dto.getTranscript());
        entity.setAudioUrl(dto.getAudioUrl());
        // If not provided in DTO, set to now
        entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        entity.setProvider(dto.getProvider());
        // to fetch agent/client from repository
        if (dto.getAgentId() != null) {
            agentRepository.findById(dto.getAgentId()).ifPresent(entity::setAgent);
        }
        if (dto.getClientId() != null) {
            clientRepository.findById(dto.getClientId()).ifPresent(entity::setClient);
        }
        VoiceLog saved = voiceLogRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public List<VoiceLogDTO> findAll() {
        return voiceLogRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public VoiceLogDTO findById(Long id) {
        return voiceLogRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> findByAgentId(Long agentId) {

        List<VoiceLog> logs= voiceLogRepository.findByAgentIdWithJoins(agentId);
        return logs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());


    }

    @Override
    public List<VoiceLogDTO> findByClientId(Long clientId) {
        return voiceLogRepository.findByClientId(clientId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        voiceLogRepository.deleteById(id);
    }

// --- Simple DTO-entity mapper ---

    private VoiceLogDTO toDto(VoiceLog entity) {
        VoiceLogDTO dto = new VoiceLogDTO();
        dto.setId(entity.getId());
        dto.setTranscript(entity.getTranscript());
        dto.setAudioUrl(entity.getAudioUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setProvider(entity.getProvider());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setExternalCallId(entity.getExternalCallId());
        dto.setRawPayload(entity.getRawPayload());
        if (entity.getAgent() != null)
            dto.setAgentId(entity.getAgent().getId());
        if (entity.getClient() != null)
            dto.setClientId(entity.getClient().getId());
        System.out.println("Transcript = " + entity.getTranscript());
        return dto;
    }
}