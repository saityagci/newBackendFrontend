package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.repository.WorkflowLogRepository;
import com.sfaai.sfaai.service.WorkflowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowLogServiceImpl implements WorkflowLogService {
    private final WorkflowLogRepository workflowLogRepository;
    private final AgentRepository agentRepository;
    private final ClientRepository clientRepository;
    private final VoiceLogRepository voiceLogRepository;

    @Override
    public WorkflowLogDTO save(WorkflowLogDTO dto) {
        WorkflowLog entity = new WorkflowLog();
        entity.setWorkflowName(dto.getWorkflowName());
        entity.setInputData(dto.getInputData());
        entity.setOutputData(dto.getOutputData());
        entity.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());

        // Link agent
        if (dto.getAgentId() != null) {
            agentRepository.findById(dto.getAgentId()).ifPresent(entity::setAgent);
        }
        // Link client
        if (dto.getClientId() != null) {
            clientRepository.findById(dto.getClientId()).ifPresent(entity::setClient);
        }
        // Link voice log (optional)
        if (dto.getVoiceLogId() != null) {
            voiceLogRepository.findById(dto.getVoiceLogId()).ifPresent(entity::setVoiceLog);
        }

        WorkflowLog saved = workflowLogRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public List<WorkflowLogDTO> findAll() {
        return workflowLogRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public WorkflowLogDTO findById(Long id) {
        return workflowLogRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public List<WorkflowLogDTO> findByAgentId(Long agentId) {
        return workflowLogRepository.findByAgentId(agentId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<WorkflowLogDTO> findByClientId(Long clientId) {
        return workflowLogRepository.findByClientId(clientId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<WorkflowLogDTO> findByVoiceLogId(Long voiceLogId) {
        return workflowLogRepository.findByVoiceLogId(voiceLogId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        workflowLogRepository.deleteById(id);
    }

    // --- Mapping ---
    private WorkflowLogDTO toDto(WorkflowLog entity) {
        WorkflowLogDTO dto = new WorkflowLogDTO();
        dto.setId(entity.getId());
        dto.setWorkflowName(entity.getWorkflowName());
        dto.setInputData(entity.getInputData());
        dto.setOutputData(entity.getOutputData());
        dto.setCreatedAt(entity.getCreatedAt());
        if (entity.getAgent() != null) dto.setAgentId(entity.getAgent().getId());
        if (entity.getClient() != null) dto.setClientId(entity.getClient().getId());
        if (entity.getVoiceLog() != null) dto.setVoiceLogId(entity.getVoiceLog().getId());
        return dto;
    }
}