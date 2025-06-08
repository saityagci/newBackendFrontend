package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.WorkflowLog;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface WorkflowLogService {
    WorkflowLog save(WorkflowLogDTO dto);

    Optional<WorkflowLogDTO> findByClient(String clientId);
}