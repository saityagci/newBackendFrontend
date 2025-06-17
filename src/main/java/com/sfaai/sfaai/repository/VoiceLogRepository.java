package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.VoiceLog;
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
public interface VoiceLogRepository extends JpaRepository<VoiceLog, Long> {
    /**
     * Find voice logs by agent ID
     * @param agentId The agent ID
     * @return List of voice logs for the agent
     */
    List<VoiceLog> findByAgentId(Long agentId);

    /**
     * Find voice logs by agent ID with pagination
     * @param agentId The agent ID
     * @param pageable Pagination information
     * @return Page of voice logs for the agent
     */
    Page<VoiceLog> findByAgentId(Long agentId, Pageable pageable);

    /**
     * Find voice logs by client ID
     * @param clientId The client ID
     * @return List of voice logs for the client
     */
    List<VoiceLog> findByClientId(Long clientId);

    /**
     * Find voice logs by client ID with pagination
     * @param clientId The client ID
     * @param pageable Pagination information
     * @return Page of voice logs for the client
     */
    Page<VoiceLog> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find voice logs by agent ID with eager loading of relationships
     * @param agentId The agent ID
     * @return List of voice logs for the agent with relationships loaded
     */
    @Query("SELECT v FROM VoiceLog v LEFT JOIN FETCH v.agent LEFT JOIN FETCH v.client WHERE v.agent.id = :agentId")
    List<VoiceLog> findByAgentIdWithJoins(@Param("agentId") Long agentId);

    /**
     * Find voice log by ID with eager loading of relationships
     * @param id The voice log ID
     * @return Optional voice log with relationships loaded
     */
    @Query("SELECT v FROM VoiceLog v LEFT JOIN FETCH v.agent LEFT JOIN FETCH v.client WHERE v.id = :id")
    Optional<VoiceLog> findByIdWithJoins(@Param("id") Long id);

    /**
     * Find voice logs by provider
     * @param provider The voice provider (e.g., "vapi", "elevenlabs")
     * @return List of voice logs for the provider
     */
    List<VoiceLog> findByProvider(String provider);

    /**
     * Find voice logs by external call ID
     * @param externalCallId The external call ID
     * @return List of voice logs with the specified external call ID
     */
    List<VoiceLog> findByExternalCallId(String externalCallId);

    /**
     * Find voice logs created between the specified dates
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of voice logs created in the date range
     */
    List<VoiceLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find voice logs by client ID and created between the specified dates
     * @param clientId The client ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of voice logs for the client created in the date range
     */
    List<VoiceLog> findByClientIdAndCreatedAtBetween(Long clientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count voice logs by client ID
     * @param clientId The client ID
     * @return Count of voice logs for the client
     */
    long countByClientId(Long clientId);

    /**
     * Count voice logs by agent ID
     * @param agentId The agent ID
     * @return Count of voice logs for the agent
     */
    long countByAgentId(Long agentId);
}