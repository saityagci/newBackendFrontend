package com.sfaai.sfaai.service;


import com.sfaai.sfaai.dto.WorkflowLogCreateDTO;
import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.WorkflowLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkflowLogService {

    /**
     * Get workflow log by id
     * @param id Workflow log id
     * @return Workflow log DTO
     */
    WorkflowLogDTO getWorkflowLogById(Long id);

    /**
     * Get all workflow logs
     * @return List of workflow log DTOs
     */
    List<WorkflowLogDTO> getAllWorkflowLogs();

    /**
     * Get workflow logs with pagination
     * @param pageable Pagination information
     * @return Page of workflow log DTOs
     */
    Page<WorkflowLogDTO> getWorkflowLogs(Pageable pageable);

    /**
     * Get workflow logs for a client
     * @param clientId Client id
     * @return List of workflow log DTOs
     */
    List<WorkflowLogDTO> getWorkflowLogsByClientId(Long clientId);

    /**
     * Get workflow logs for an agent
     * @param agentId Agent id
     * @return List of workflow log DTOs
     */
    List<WorkflowLogDTO> getWorkflowLogsByAgentId(Long agentId);

    /**
     * Get workflow logs for a voice log
     * @param voiceLogId Voice log id
     * @return List of workflow log DTOs
     */
    List<WorkflowLogDTO> getWorkflowLogsByVoiceLogId(Long voiceLogId);

    /**
     * Get workflow logs by name
     * @param workflowName Workflow name
     * @return List of workflow log DTOs
     */
    List<WorkflowLogDTO> getWorkflowLogsByName(String workflowName);

    /**
     * Create a new workflow log
     * @param dto Workflow log create DTO
     * @return Created workflow log DTO
     */
    WorkflowLogDTO createWorkflowLog(WorkflowLogCreateDTO dto);

    /**
     * Update workflow log status
     * @param id Workflow log id
     * @param status New status
     * @param result Result data (if completed)
     * @param errorMessage Error message (if failed)
     * @return Updated workflow log DTO
     */
    WorkflowLogDTO updateWorkflowLogStatus(Long id, WorkflowLog.Status status, String result, String errorMessage);

    /**
     * Delete a workflow log
     * @param id Workflow log id
     */
    void deleteWorkflowLog(Long id);
}
