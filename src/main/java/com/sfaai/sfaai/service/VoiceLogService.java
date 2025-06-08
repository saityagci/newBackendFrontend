package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VoiceLogDTO;

import java.util.List;

public interface VoiceLogService {
    VoiceLogDTO save(VoiceLogDTO dto);
    List<VoiceLogDTO> findAll();
}