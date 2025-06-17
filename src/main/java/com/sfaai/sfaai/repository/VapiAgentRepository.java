package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAgent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vapi agent entities
 */
@Repository
public interface VapiAgentRepository extends JpaRepository<VapiAgent, Long>, JpaSpecificationExecutor<VapiAgent> {

    /**
     * Find Vapi agent by external Vapi ID
     * @param vapiAgentId External Vapi agent ID
     * @return Optional VapiAgent
     */
    Optional<VapiAgent> findByVapiAgentId(String vapiAgentId);

    /**
     * Find Vapi agents by client
     * @param client The client
     * @return List of Vapi agents for the client
     */
    List<VapiAgent> findByClient(Client client);

    /**
     * Find Vapi agents by client ID
     * @param clientId The client ID
     * @return List of Vapi agents for the client
     */
    List<VapiAgent> findByClientId(Long clientId);

    /**
     * Find Vapi agents by client ID with pagination
     * @param clientId The client ID
     * @param pageable Pagination information
     * @return Page of Vapi agents for the client
     */
    Page<VapiAgent> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find Vapi agents by name containing the given text (case insensitive)
     * @param name The name fragment to search for
     * @return List of matching Vapi agents
     */
    List<VapiAgent> findByNameContainingIgnoreCase(String name);

    /**
     * Count Vapi agents by client ID
     * @param clientId The client ID
     * @return Count of Vapi agents for the client
     */
    long countByClientId(Long clientId);
}
