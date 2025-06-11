package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VoiceLogDTO;

import java.util.List;

public interface VoiceLogService {
    VoiceLogDTO save(VoiceLogDTO dto);
    List<VoiceLogDTO> findAll();
    VoiceLogDTO findById(Long id);
    List<VoiceLogDTO> findByAgentId(Long agentId);
    List<VoiceLogDTO> findByClientId(Long clientId);
    void delete(Long id); // Optional, based on your data retention policy
}
