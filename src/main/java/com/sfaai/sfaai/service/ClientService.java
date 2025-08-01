package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.ClientUpdateDTO;
import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for client/user management operations
 */
public interface ClientService {
    /**
     * Create a new client
     * @param dto Client creation data
     * @return Created client DTO
     */
    ClientDTO save(ClientCreateDTO dto);

    /**
     * Update an existing client
     * @param id Client ID to update
     * @param dto Updated client data
     * @return Updated client DTO
     */
    ClientDTO update(Long id, ClientDTO dto);

    /**
     * Update an existing client with partial data
     * @param id Client ID to update
     * @param dto Updated client data (partial update)
     * @return Updated client DTO
     */
    ClientDTO update(Long id, ClientUpdateDTO dto);

    /**
     * Find all clients
     * @return List of all client DTOs
     */
    List<ClientDTO> findAll();

    /**
     * Find all clients with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return List of client DTOs for the requested page
     * @deprecated Use {@link #findAll(Pageable)} instead for paginated responses
     */
    @Deprecated
    List<ClientDTO> findAll(int page, int size);

    /**
     * Find all clients with pagination
     * @param pageable Pagination information
     * @return Page of client DTOs with pagination metadata (total elements, total pages, etc.)
     */
    Page<ClientDTO> findAll(Pageable pageable);

    /**
     * Find client by ID
     * @param id Client ID
     * @return Client DTO or throws exception if not found
     */
    ClientDTO findById(Long id);

    /**
     * Find client by email
     * @param email Client email
     * @return Client DTO or throws exception if not found
     */
    ClientDTO findByEmail(String email);

    /**
     * Delete a client
     * @param id Client ID to delete
     */
    void delete(Long id);

    /**
     * Update a client's password
     * @param id Client ID to update
     * @param newPassword New password
     */
    void updatePassword(Long id, String newPassword);

    /**
     * Update a client's password with current password verification
     * @param id Client ID to update
     * @param currentPassword Current password for verification
     * @param newPassword New password
     */
    void updatePasswordWithVerification(Long id, String currentPassword, String newPassword);

    /**
     * Get client by API key
     * @param apiKey Client API key
     * @return Client DTO or throws exception if not found
     */
    ClientDTO getClientByApiKey(String apiKey);

    /**
     * Check if a client exists with the given email
     * @param email The email to check
     * @return true if a client with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find clients by role
     * @param role The role to search for (e.g., "USER", "ADMIN")
     * @return List of clients with the specified role
     */
    List<ClientDTO> findByRole(String role);

    /**
     * Assign a Vapi assistant to a client
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    ClientDTO assignVapiAssistant(Long clientId, String vapiAssistantId);

    /**
     * Unassign (remove) the Vapi assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO with vapiAssistantId set to null
     */
    ClientDTO unassignVapiAssistant(Long clientId);

    /**
     * Add a Vapi assistant to a client's list of assistants
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    ClientDTO addVapiAssistant(Long clientId, String vapiAssistantId);

    /**
     * Remove a Vapi assistant from a client's list of assistants
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    ClientDTO removeVapiAssistant(Long clientId, String vapiAssistantId);

    /**
     * Find all clients assigned to a specific Vapi assistant
     * @param assistantId The Vapi assistant ID
     * @return List of client DTOs assigned to the assistant
     */
    List<ClientDTO> findClientsByAssistantId(String assistantId);

    /**
     * Get all Vapi assistants assigned to a client
     * @param clientId The client ID
     * @return List of VapiAssistantDTO objects assigned to the client
     */
    List<VapiAssistantDTO> getAssignedVapiAssistants(Long clientId);

    /**
     * Assign an ElevenLabs assistant to a client
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    ClientDTO assignElevenLabsAssistant(Long clientId, String elevenLabsAssistantId);

    /**
     * Unassign (remove) the ElevenLabs assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO with elevenLabsAssistantId set to null
     */
    ClientDTO unassignElevenLabsAssistant(Long clientId);

    /**
     * Add an ElevenLabs assistant to a client's list of assistants
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    ClientDTO addElevenLabsAssistant(Long clientId, String elevenLabsAssistantId);

    /**
     * Remove an ElevenLabs assistant from a client's list of assistants
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    ClientDTO removeElevenLabsAssistant(Long clientId, String elevenLabsAssistantId);

    /**
     * Find all clients assigned to a specific ElevenLabs assistant
     * @param assistantId The ElevenLabs assistant ID
     * @return List of client DTOs assigned to the assistant
     */
    List<ClientDTO> findClientsByElevenLabsAssistantId(String assistantId);

    /**
     * Get all ElevenLabs assistants assigned to a client
     * @param clientId The client ID
     * @return List of ElevenLabsAssistantDTO objects assigned to the client
     */
    List<ElevenLabsAssistantDTO> getAssignedElevenLabsAssistants(Long clientId);
}
