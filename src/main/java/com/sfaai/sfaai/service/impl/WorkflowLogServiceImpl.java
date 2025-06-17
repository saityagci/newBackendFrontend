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
        return workflowLogMapper.toDtoList(workflowLogRepository.findByClientId(clientId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByAgentId(Long agentId) {
        return workflowLogMapper.toDtoList(workflowLogRepository.findByAgentId(agentId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByVoiceLogId(Long voiceLogId) {
        return workflowLogMapper.toDtoList(workflowLogRepository.findByVoiceLogId(voiceLogId));
    }


    @Override
    @Transactional(readOnly = true)
    public List<WorkflowLogDTO> getWorkflowLogsByName(String workflowName) {
        return workflowLogMapper.toDtoList(workflowLogRepository.findByWorkflowName(workflowName));
    }


    @Override
    @Transactional
    public WorkflowLogDTO createWorkflowLog(WorkflowLogCreateDTO dto) {
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
        if (!workflowLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workflow log not found with id: " + id);
        }
        workflowLogRepository.deleteById(id);
    }

}

