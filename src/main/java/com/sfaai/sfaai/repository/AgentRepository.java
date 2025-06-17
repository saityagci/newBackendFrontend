package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import com.sfaai.sfaai.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long>, JpaSpecificationExecutor<Agent> {
    /**
     * Find agents by client
     * @param client The client
     * @return List of agents for the client
     */
    List<Agent> findByClient(Client client);

    /**
     * Find agents by client ID
     * @param clientId The client ID
     * @return List of agents for the client
     */
    List<Agent> findByClientId(Long clientId);

    /**
     * Find agents by client ID with pagination
     * @param clientId The client ID
     * @param pageable Pagination information
     * @return Page of agents for the client
     */
    Page<Agent> findByClientId(Long clientId, Pageable pageable);

    /**
     * Find agents by type
     * @param type The agent type
     * @return List of agents of the specified type
     */
    List<Agent> findByType(AgentType type);

    /**
     * Find agents by status
     * @param status The agent status
     * @return List of agents with the specified status
     */
    List<Agent> findByStatus(AgentStatus status);

    /**
     * Find agents by client ID and status
     * @param clientId The client ID
     * @param status The agent status
     * @return List of agents for the client with the specified status
     */
    List<Agent> findByClientIdAndStatus(Long clientId, AgentStatus status);

    /**
     * Find agents by name containing the given text (case insensitive)
     * @param name The name fragment to search for
     * @return List of matching agents
     */
    List<Agent> findByNameContainingIgnoreCase(String name);

    /**
     * Get agent with all relationships eagerly loaded
     * @param id The agent ID
     * @return Optional agent with relationships loaded
     */
    @Query("SELECT a FROM Agent a LEFT JOIN FETCH a.client WHERE a.id = :id")
    Optional<Agent> findByIdWithClient(@Param("id") Long id);

    /**
     * Count agents by client ID
     * @param clientId The client ID
     * @return Count of agents for the client
     */
    long countByClientId(Long clientId);

    /**
     * Count agents by status
     * @param status The agent status
     * @return Count of agents with the specified status
     */
    long countByStatus(AgentStatus status);
}
