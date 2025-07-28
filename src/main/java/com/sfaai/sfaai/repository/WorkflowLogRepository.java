package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.WorkflowLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowLogRepository extends JpaRepository<WorkflowLog, Long> {

    /**
     * Find workflow logs by client ID
     * @param clientId The client ID
     * @return List of workflow logs for the client
     */
    List<WorkflowLog> findByClient_Id(Long clientId);

    /**
     * Find workflow logs by client ID with pagination
     * @param clientId The client ID
     * @param pageable Pagination information
     * @return Page of workflow logs for the client
     */
    Page<WorkflowLog> findByClient_Id(Long clientId, Pageable pageable);

    /**
     * Find workflow logs by voice log ID
     * @param voiceLogId The voice log ID
     * @return List of workflow logs for the voice log
     */
    List<WorkflowLog> findByVoiceLog_Id(Long voiceLogId);

    /**
     * Find workflow logs by agent ID
     * @param agentId The agent ID
     * @return List of workflow logs for the agent
     */
    List<WorkflowLog> findByAgent_Id(Long agentId);

    /**
     * Find workflow logs by agent ID with pagination
     * @param agentId The agent ID
     * @param pageable Pagination information
     * @return Page of workflow logs for the agent
     */
    Page<WorkflowLog> findByAgent_Id(Long agentId, Pageable pageable);

    /**
     * Find workflow logs by workflow name
     * @param workflowName The workflow name
     * @return List of workflow logs with the specified name
     */
    List<WorkflowLog> findByWorkflowName(String workflowName);

    /**
     * Find workflow logs by workflow name containing the given text (case insensitive)
     * @param nameFragment The name fragment to search for
     * @return List of matching workflow logs
     */
    List<WorkflowLog> findByWorkflowNameContainingIgnoreCase(String nameFragment);

    /**
     * Find workflow log by ID with eager loading of relationships
     * @param id The workflow log ID
     * @return Optional workflow log with relationships loaded
     */
    @Query("SELECT w FROM WorkflowLog w LEFT JOIN FETCH w.agent LEFT JOIN FETCH w.client LEFT JOIN FETCH w.voiceLog WHERE w.id = :id")
    Optional<WorkflowLog> findByIdWithJoins(@Param("id") Long id);

    /**
     * Find workflow logs by agent ID with eager loading of relationships
     * @param agentId The agent ID
     * @return List of workflow logs for the agent with relationships loaded
     */
    @Query("SELECT w FROM WorkflowLog w LEFT JOIN FETCH w.agent LEFT JOIN FETCH w.client WHERE w.agent.id = :agentId")
    List<WorkflowLog> findByAgentIdWithJoins(@Param("agentId") Long agentId);

    /**
     * Find workflow logs created between the specified dates
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of workflow logs created in the date range
     */
    List<WorkflowLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find workflow logs by client ID and created between the specified dates
     * @param clientId The client ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of workflow logs for the client created in the date range
     */
    List<WorkflowLog> findByClient_IdAndCreatedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count workflow logs by client ID
     * @param clientId The client ID
     * @return Count of workflow logs for the client
     */
    long countByClient_Id(Long clientId);

    /**
     * Count workflow logs by agent ID
     * @param agentId The agent ID
     * @return Count of workflow logs for the agent
     */
    long countByAgent_Id(Long agentId);

    /**
     * Count workflow logs by voice log ID
     * @param voiceLogId The voice log ID
     * @return Count of workflow logs for the voice log
     */
    long countByVoiceLog_Id(Long voiceLogId);
}