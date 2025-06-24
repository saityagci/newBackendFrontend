package com.sfaai.sfaai.service.impl;


import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class VoiceLogServiceImpl implements VoiceLogService {

    //private final VoiceLogRepository voiceLogRepository;
    private final VoiceLogMapper voiceLogMapper;
    private final VoiceLogRepository voiceLogRepository;

    public VoiceLogServiceImpl(VoiceLogMapper voiceLogMapper, VoiceLogRepository voiceLogRepository) {
        this.voiceLogMapper = voiceLogMapper;
        this.voiceLogRepository = voiceLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceLogDTO getVoiceLogById(Long id) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));
        return voiceLogMapper.toDto(voiceLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getAllVoiceLogs() {
        List<VoiceLog> voiceLogs = voiceLogRepository.findAll();
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceLogDTO> getVoiceLogs(Pageable pageable) {
        Page<VoiceLog> voiceLogs = voiceLogRepository.findAll(pageable);
        return voiceLogs.map(voiceLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByClientId(Long clientId) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByClientId(clientId);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByAgentId(Long agentId) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByAgentId(agentId);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByCreatedAtBetween(startDate, endDate);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional
    public VoiceLogDTO createVoiceLog(VoiceLogCreateDTO dto) {
        VoiceLog voiceLog = voiceLogMapper.createEntityFromDto(dto);
        VoiceLog savedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(savedVoiceLog);
    }

    @Override
    public VoiceLogDTO save(VoiceLogCreateDTO dto) {
        VoiceLog voiceLog = voiceLogMapper.createEntityFromDto(dto);
        VoiceLog savedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(savedVoiceLog);
    }

    @Override
    @Transactional
    public VoiceLogDTO updateVoiceLogStatus(Long id, VoiceLog.Status status) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));

        voiceLog.setStatus(status);

        // If completed, set end time if not already set
        if (status == VoiceLog.Status.COMPLETED && voiceLog.getEndedAt() == null) {
            voiceLog.setEndedAt(LocalDateTime.now());
        }

        VoiceLog updatedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(updatedVoiceLog);
    }

    @Override
    @Transactional
    public void deleteVoiceLog(Long id) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));

        voiceLogRepository.delete(voiceLog);
    }
}


