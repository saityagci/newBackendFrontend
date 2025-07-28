package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.WorkflowLogCreateDTO;
import com.sfaai.sfaai.dto.WorkflowLogDTO;

import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.WorkflowLogMapper;
import com.sfaai.sfaai.repository.WorkflowLogRepository;
import com.sfaai.sfaai.service.WorkflowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WorkflowLogServiceImpl implements WorkflowLogService {

    private final WorkflowLogRepository workflowLogRepository;
    private final WorkflowLogMapper workflowLogMapper;

    @Override
    @Transactional(readOnly = true)
    public WorkflowLogDTO getWorkflowLogById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        WorkflowLog workflowLog = workflowLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow log not found with id: " + id));
        return workflowLogMapper.toDto(workflowLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getAllWorkflowLogs() {
        return workflowLogMapper.toDtoList(workflowLogRepository.findAll());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<WorkflowLogDTO> getWorkflowLogs(Pageable pageable) {
        return workflowLogRepository.findAll(pageable)
                .map(workflowLogMapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByClientId(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        return workflowLogMapper.toDtoList(workflowLogRepository.findByClient_Id(clientId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByAgentId(Long agentId) {
        if (agentId == null) {
            throw new IllegalArgumentException("Agent ID cannot be null");
        }
        return workflowLogMapper.toDtoList(workflowLogRepository.findByAgent_Id(agentId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByVoiceLogId(Long voiceLogId) {
        if (voiceLogId == null) {
            throw new IllegalArgumentException("Voice Log ID cannot be null");
        }
        return workflowLogMapper.toDtoList(workflowLogRepository.findByVoiceLog_Id(voiceLogId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByName(String workflowName) {
        return workflowLogMapper.toDtoList(workflowLogRepository.findByWorkflowName(workflowName));
    }


    @Override
    @Transactional
    public WorkflowLogDTO createWorkflowLog(WorkflowLogCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (dto.getClientId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        
        WorkflowLog workflowLog = workflowLogMapper.createEntityFromDto(dto);
        workflowLog.setStatus(WorkflowLog.Status.INITIATED);
        return workflowLogMapper.toDto(workflowLogRepository.save(workflowLog));
    }


    @Override
    @Transactional
    public WorkflowLogDTO updateWorkflowLogStatus(Long id, WorkflowLog.Status status, String result, String errorMessage) {
        WorkflowLog workflowLog = workflowLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow log not found with id: " + id));

        workflowLog.setStatus(status);
        if (result != null) {
            workflowLog.setResult(result);
        }
        if (errorMessage != null) {
            workflowLog.setErrorMessage(errorMessage);
        }

        return workflowLogMapper.toDto(workflowLogRepository.save(workflowLog));
    }


    @Override
    @Transactional
    public void deleteWorkflowLog(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!workflowLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workflow log not found with id: " + id);
        }
        workflowLogRepository.deleteById(id);
    }

}

