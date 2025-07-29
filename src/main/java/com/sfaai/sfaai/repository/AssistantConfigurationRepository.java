package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.AssistantConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistantConfigurationRepository extends JpaRepository<AssistantConfiguration, Long> {

    Optional<AssistantConfiguration> findByAssistantId(String assistantId);

    List<AssistantConfiguration> findByClientId(String clientId);

    List<AssistantConfiguration> findByStatus(AssistantConfiguration.Status status);

    @Query("SELECT ac FROM AssistantConfiguration ac WHERE ac.assistantId = :assistantId AND ac.clientId = :clientId")
    Optional<AssistantConfiguration> findByAssistantIdAndClientId(@Param("assistantId") String assistantId, @Param("clientId") String clientId);

    Page<AssistantConfiguration> findByClientId(String clientId, Pageable pageable);

    @Query("SELECT ac FROM AssistantConfiguration ac WHERE ac.status = :status ORDER BY ac.updatedAt DESC")
    Page<AssistantConfiguration> findByStatusOrderByUpdatedAtDesc(@Param("status") AssistantConfiguration.Status status, Pageable pageable);

    @Query("SELECT ac FROM AssistantConfiguration ac WHERE " +
           "(:subject IS NULL OR LOWER(ac.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:clientId IS NULL OR ac.clientId = :clientId) AND " +
           "(:status IS NULL OR ac.status = :status) AND " +
           "(:assistantId IS NULL OR ac.assistantId = :assistantId) " +
           "ORDER BY ac.updatedAt DESC")
    Page<AssistantConfiguration> findByFilters(@Param("subject") String subject, 
                                              @Param("clientId") String clientId, 
                                              @Param("status") AssistantConfiguration.Status status, 
                                              @Param("assistantId") String assistantId, 
                                              Pageable pageable);

    long countByStatus(AssistantConfiguration.Status status);

    /**
     * Find all configurations by client ID with pagination (ordered by updated date desc)
     */
    Page<AssistantConfiguration> findByClientIdOrderByUpdatedAtDesc(String clientId, Pageable pageable);

    /**
     * Find all configurations by client ID and status (ordered by updated date desc)
     */
    List<AssistantConfiguration> findByClientIdAndStatusOrderByUpdatedAtDesc(String clientId, AssistantConfiguration.Status status);

    /**
     * Count configurations by client ID
     */
    long countByClientId(String clientId);

    /**
     * Count configurations by client ID and status
     */
    long countByClientIdAndStatus(String clientId, AssistantConfiguration.Status status);
} 