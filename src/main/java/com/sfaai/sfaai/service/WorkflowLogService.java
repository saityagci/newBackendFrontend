package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import java.util.List;

public interface WorkflowLogService {
    WorkflowLogDTO save(WorkflowLogDTO dto);
    List<WorkflowLogDTO> findAll();
    WorkflowLogDTO findById(Long id);
    List<WorkflowLogDTO> findByAgentId(Long agentId);
    List<WorkflowLogDTO> findByClientId(Long clientId);
    List<WorkflowLogDTO> findByVoiceLogId(Long voiceLogId);
    void delete(Long id);
}