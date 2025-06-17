package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VapiAgentRequestDTO;
import com.sfaai.sfaai.dto.VapiAgentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for Vapi voice agent operations
 */
public interface VapiAgentService {

    /**
     * Create a new Vapi voice agent
     * @param requestDTO Agent creation data
     * @return Created agent response
     */
    VapiAgentResponseDTO createVapiAgent(VapiAgentRequestDTO requestDTO);

    /**
     * Get Vapi agent by ID
     * @param id Agent ID
     * @return Agent response DTO
     */
    VapiAgentResponseDTO getVapiAgent(Long id);

    /**
     * Get Vapi agent by external Vapi ID
     * @param vapiAgentId External Vapi agent ID
     * @return Agent response DTO
     */
    VapiAgentResponseDTO getVapiAgentByExternalId(String vapiAgentId);

    /**
     * Get all Vapi agents with pagination
     * @param pageable Pagination information
     * @return Page of agent response DTOs
     */
    Page<VapiAgentResponseDTO> getAllVapiAgents(Pageable pageable);

    /**
     * Get all Vapi agents for a specific client
     * @param clientId Client ID
     * @return List of agent response DTOs
     */
    List<VapiAgentResponseDTO> getVapiAgentsByClientId(Long clientId);

    /**
     * Delete a Vapi agent
     * @param id Agent ID
     */
    void deleteVapiAgent(Long id);
}
