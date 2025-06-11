package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.WorkflowLogRepository;
import com.sfaai.sfaai.service.WorkflowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowLogServiceImpl implements WorkflowLogService {

    private final WorkflowLogRepository workflowLogRepository;
    private final ClientRepository clientRepository;


    @Override
    public WorkflowLog save(WorkflowLogDTO dto) {
        // Fetch the Client entity from the database using the provided clientId
        Client client = clientRepository.findById(Long.valueOf(dto.getClientId()))
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + dto.getClientId()));

        // Create and save WorkflowLog entity
        WorkflowLog log = WorkflowLog.builder()
                .workflowName(dto.getWorkflowName())
                .inputJson(dto.getInputJson())
                .outputJson(dto.getOutputJson())
                .createdAt(LocalDateTime.now())
                .build();

        return workflowLogRepository.save(log);
    }



    @Override
    public Optional<WorkflowLogDTO> findByClient(String clientId) {
        return workflowLogRepository.findByClientId(Long.valueOf(clientId));
    }
}