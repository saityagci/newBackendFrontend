package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.WorkflowLogCreateDTO;
import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowLogMapper implements EntityMapper<WorkflowLogDTO, WorkflowLog> {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private VoiceLogRepository voiceLogRepository;

    @Override
    public WorkflowLogDTO toDto(WorkflowLog entity) {
        if (entity == null) {
            return null;
        }

        return WorkflowLogDTO.builder()
                .id(entity.getId())
                .agentId(entity.getAgent().getId())
                .clientId(entity.getClient().getId())
                .voiceLogId(entity.getVoiceLog() != null ? entity.getVoiceLog().getId() : null)
                .workflowName(entity.getWorkflowName())
                .inputData(entity.getInputData())
                .outputData(entity.getOutputData())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public WorkflowLog toEntity(WorkflowLogDTO dto) {
        if (dto == null) {
            return null;
        }

        WorkflowLog workflowLog = WorkflowLog.builder()
                .workflowName(dto.getWorkflowName())
                .inputData(dto.getInputData())
                .outputData(dto.getOutputData())
                .status(WorkflowLog.Status.INITIATED) // Default status
                .build();

        if (dto.getId() != null) {
            workflowLog.setId(dto.getId());
        }

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + dto.getClientId()));
            workflowLog.setClient(client);
        }

        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + dto.getAgentId()));
            workflowLog.setAgent(agent);
        }

        if (dto.getVoiceLogId() != null) {
            VoiceLog voiceLog = voiceLogRepository.findById(dto.getVoiceLogId())
                    .orElseThrow(() -> new RuntimeException("VoiceLog not found with id: " + dto.getVoiceLogId()));
            workflowLog.setVoiceLog(voiceLog);
        }

        return workflowLog;
    }

    public WorkflowLog createEntityFromDto(WorkflowLogCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        WorkflowLog workflowLog = WorkflowLog.builder()
                .workflowName(dto.getWorkflowName())
                .inputData(dto.getInputData())
                .outputData(dto.getOutputData())
                .status(WorkflowLog.Status.INITIATED) // Default status
                .build();

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + dto.getClientId()));
            workflowLog.setClient(client);
        }

        if (dto.getAgentId() != null) {
            Agent agent = agentRepository.findById(dto.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + dto.getAgentId()));
            workflowLog.setAgent(agent);
        }

        if (dto.getVoiceLogId() != null) {
            VoiceLog voiceLog = voiceLogRepository.findById(dto.getVoiceLogId())
                    .orElseThrow(() -> new RuntimeException("VoiceLog not found with id: " + dto.getVoiceLogId()));
            workflowLog.setVoiceLog(voiceLog);
        }

        return workflowLog;
    }

    @Override
    public List<WorkflowLogDTO> toDtoList(List<WorkflowLog> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowLog> toEntityList(List<WorkflowLogDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
