package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service for agent management operations
 */
public interface AgentService {
    /**
     * Create a new agent
     * @param dto Agent creation data
     * @return Created agent DTO
     */
    AgentDTO createAgent(AgentCreateDTO dto);

    /**
     * Get agent by ID
     * @param id Agent ID
     * @return Agent DTO or throws exception if not found
     */
    AgentDTO getAgent(Long id);

    /**
     * Get agent by id (alias for getAgent)
     * @param id Agent id
     * @return Agent DTO
     */
    default AgentDTO getAgentById(Long id) {
        return getAgent(id);
    }

    /**
     * Get all agents with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return List of agent DTOs
     */
    List<AgentDTO> getAllAgents(int page, int size);

    /**
     * Get all agents (no pagination)
     * @return List of all agent DTOs
     */
    List<AgentDTO> getAllAgents();

    /**
     * Get agents with pagination and optional filtering
     * @param pageable Pagination information
     * @param filters Map of filter criteria (key = field name, value = filter value)
     * @return Page of agent DTOs
     */
    Page<AgentDTO> getAgents(Pageable pageable, Map<String, Object> filters);

    /**
     * Get agents with pagination
     * @param pageable Pagination information
     * @return Page of agent DTOs
     */
    default Page<AgentDTO> getAgents(Pageable pageable) {
        return getAgents(pageable, null);
    }

    /**
     * Update an existing agent
     * @param id Agent ID to update
     * @param dto Updated agent data
     * @return Updated agent DTO
     * @throws ResourceNotFoundException if agent not found
     */
    AgentDTO updateAgent(Long id, AgentDTO dto);

    /**
     * Delete an agent
     * @param id Agent ID to delete
     * @throws ResourceNotFoundException if agent not found
     */
    void deleteAgent(Long id);

    /**
     * Get agents for a specific client
     * @param clientId Client ID
     * @return List of agent DTOs
     */
    List<AgentDTO> getAgentsByClientId(Long clientId);

    /**
     * Get agents for a specific client with pagination
     * @param clientId Client ID
     * @param pageable Pagination information
     * @return Page of agent DTOs
     */
    Page<AgentDTO> getAgentsByClientId(Long clientId, Pageable pageable);

    /**
     * Get agents by type
     * @param type Agent type
     * @return List of agent DTOs
     */
    List<AgentDTO> getAgentsByType(AgentType type);

    /**
     * Get agents by status
     * @param status Agent status
     * @return List of agent DTOs
     */
    List<AgentDTO> getAgentsByStatus(AgentStatus status);

    /**
     * Count agents by client ID
     * @param clientId Client ID
     * @return Count of agents for the client
     */
    long countAgentsByClientId(Long clientId);

    /**
     * Update agent status
     * @param id Agent ID
     * @param status New status
     * @return Updated agent DTO
     * @throws ResourceNotFoundException if agent not found
     */
    AgentDTO updateAgentStatus(Long id, AgentStatus status);
}
