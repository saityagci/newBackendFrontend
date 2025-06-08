package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceLogServiceImpl implements VoiceLogService {

    private final VoiceLogRepository repo;

    private VoiceLogDTO toDto(VoiceLog log) {
        return VoiceLogDTO.builder()
                .id(log.getId())
                .phoneNumber(log.getPhoneNumber())
                .transcript(log.getTranscript())
                .callTime(log.getCallTime())
                .source(log.getSource())
                .build();
    }

    private VoiceLog toEntity(VoiceLogDTO dto) {
        return VoiceLog.builder()
                .id(dto.getId())
                .phoneNumber(dto.getPhoneNumber())
                .transcript(dto.getTranscript())
                .callTime(dto.getCallTime())
                .source(dto.getSource())
                .build();
    }

    @Override
    public VoiceLogDTO save(VoiceLogDTO dto) {
        return toDto(repo.save(toEntity(dto)));
    }

    @Override
    public List<VoiceLogDTO> findAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }
}