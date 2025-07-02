package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.ClientMapper;
import com.sfaai.sfaai.mapper.VapiAssistantMapper;
import com.sfaai.sfaai.mapper.ElevenLabsAssistantMapper;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.service.ClientService;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.ElevenLabsAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final VapiAssistantRepository vapiAssistantRepository;
    private final ElevenLabsAssistantRepository elevenLabsAssistantRepository;
    private final ClientMapper clientMapper;
    private final VapiAssistantMapper vapiAssistantMapper;
    private final ElevenLabsAssistantMapper elevenLabsAssistantMapper;
    private final PasswordEncoder passwordEncoder;
    private final VapiAgentService vapiAgentService;
    private final ElevenLabsAssistantService elevenLabsAssistantService;

    /**
     * Create a new client
     * @param dto Client creation data
     * @return Created client DTO
     */
    @Override
    public ClientDTO save(ClientCreateDTO dto) {
        // Check if email already exists
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Generate a unique API key for the new client
        String apiKey = generateUniqueApiKey();

        // Map DTO to entity
        Client client = Client.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : "USER")
                .password(passwordEncoder.encode(dto.getPassword()))
                .apiKey(apiKey)
                .build();

        // Save to database
        Client saved = clientRepository.save(client);
        return clientMapper.toDto(saved);
    }

    /**
     * Update an existing client
     * @param id Client ID to update
     * @param dto Updated client data
     * @return Updated client DTO
     */
    @Override
    public ClientDTO update(Long id, ClientDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Update fields
        client.setFullName(dto.getFullName());
        if (dto.getPhone() != null) {
            client.setPhone(dto.getPhone());
        }

        // Only allow role updates if authorized (this would be checked at controller level)
        if (dto.getRole() != null) {
            client.setRole(dto.getRole());
        }

        // Save updated client
        Client updated = clientRepository.save(client);
        return clientMapper.toDto(updated);
    }

    /**
     * Find all clients
     * @return List of all client DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find all clients with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return List of client DTOs for the requested page
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findAll(pageable).getContent().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find all clients with pagination
     * @param pageable Pagination information
     * @return Page of client DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClientDTO> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(clientMapper::toDto);
    }

    /**
     * Find client by ID
     * @param id Client ID
     * @return Client DTO or throws exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ClientDTO findById(Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    /**
     * Find client by email
     * @param email Client email
     * @return Client DTO or throws exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ClientDTO findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with email: " + email));
    }

    /**
     * Get client by API key
     * @param apiKey Client API key
     * @return Client DTO or throws exception if not found
     */
    @Override
    @Transactional(readOnly = true)
    public ClientDTO getClientByApiKey(String apiKey) {
        return clientRepository.findByApiKey(apiKey)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with API key: " + apiKey));
    }

    /**
     * Delete a client
     * @param id Client ID to delete
     */
    @Override
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    /**
     * Check if a client exists with the given email
     * @param email The email to check
     * @return true if a client with the email exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    /**
     * Find clients by role
     * @param role The role to search for
     * @return List of clients with the specified role
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findByRole(String role) {
        return clientRepository.findByRole(role).stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Assign a Vapi assistant to a client
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO assignVapiAssistant(Long clientId, String vapiAssistantId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        client.setVapiAssistantId(vapiAssistantId);

        // Get or create the assistant entity
        VapiAssistant assistant = vapiAssistantRepository.findById(vapiAssistantId)
                .orElseGet(() -> updateLocalAssistantFromVapi(vapiAssistantId));

        if (assistant != null) {
            // Check if this assistant is already associated with this client
            boolean alreadyAssociated = client.getVapiAssistants().stream()
                    .anyMatch(a -> a.getAssistantId().equals(vapiAssistantId));

            if (!alreadyAssociated) {
                // Set the client reference on the assistant
                assistant.setClient(client);

                // Save the assistant first to ensure it exists
                vapiAssistantRepository.save(assistant);

                // Add to the collection using the helper method to maintain bidirectional relationship
                client.addVapiAssistant(assistant);
            }
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Unassign (remove) the Vapi assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO with vapiAssistantId set to null
     */
    @Override
    @Transactional
    public ClientDTO unassignVapiAssistant(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        String oldAssistantId = client.getVapiAssistantId();
        client.setVapiAssistantId(null);

        // Remove the assistant from the client's collection if it exists
        if (oldAssistantId != null) {
            client.getVapiAssistants().stream()
                    .filter(assistant -> assistant.getAssistantId().equals(oldAssistantId))
                    .findFirst()
                    .ifPresent(client::removeVapiAssistant);
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Add a Vapi assistant to a client's list of assistants
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO addVapiAssistant(Long clientId, String vapiAssistantId) {
        log.info("Adding Vapi assistant {} to client {}", vapiAssistantId, clientId);

        // Verify the client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Get or create the assistant entity
        VapiAssistant assistant = vapiAssistantRepository.findById(vapiAssistantId)
                .orElseGet(() -> {
                    // Try to create it if it doesn't exist
                    VapiAssistant newAssistant = updateLocalAssistantFromVapi(vapiAssistantId);
                    if (newAssistant == null) {
                        throw new ResourceNotFoundException("Vapi assistant not found with ID: " + vapiAssistantId);
                    }
                    return newAssistant;
                });

        // Check if this assistant is already associated with this client
        boolean alreadyAssociated = client.getVapiAssistants().stream()
                .anyMatch(a -> a.getAssistantId().equals(vapiAssistantId));

        if (!alreadyAssociated) {
            // Set the client reference on the assistant
            assistant.setClient(client);

            // Save the assistant first to ensure it exists
            vapiAssistantRepository.save(assistant);

            // Add to the collection using the helper method to maintain bidirectional relationship
            client.addVapiAssistant(assistant);
            log.info("Added Vapi assistant {} to client {}'s list", vapiAssistantId, clientId);
        } else {
            log.info("Vapi assistant {} already assigned to client {}", vapiAssistantId, clientId);
        }

        // If the client doesn't have a primary assistant, set this one as primary
        if (client.getVapiAssistantId() == null) {
            client.setVapiAssistantId(vapiAssistantId);
            log.info("Set Vapi assistant {} as primary assistant for client {}", vapiAssistantId, clientId);
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Remove a Vapi assistant from a client's list of assistants
     * @param clientId Client ID
     * @param vapiAssistantId Vapi assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO removeVapiAssistant(Long clientId, String vapiAssistantId) {
        log.info("Removing Vapi assistant {} from client {}", vapiAssistantId, clientId);

        // Verify the client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Find the assistant in the client's collection
        VapiAssistant assistantToRemove = client.getVapiAssistants().stream()
                .filter(assistant -> assistant.getAssistantId().equals(vapiAssistantId))
                .findFirst()
                .orElse(null);

        // Remove if found
        if (assistantToRemove != null) {
            client.removeVapiAssistant(assistantToRemove);
            log.info("Removed Vapi assistant {} from client {}'s list", vapiAssistantId, clientId);
        } else {
            log.info("Vapi assistant {} was not in client {}'s list", vapiAssistantId, clientId);
        }

        // If this was the primary assistant, update the primary assistant
        if (vapiAssistantId.equals(client.getVapiAssistantId())) {
            // If there are other assistants, set the first one as primary
            if (!client.getVapiAssistants().isEmpty()) {
                client.setVapiAssistantId(client.getVapiAssistants().get(0).getAssistantId());
                log.info("Set Vapi assistant {} as new primary assistant for client {}", 
                        client.getVapiAssistantId(), clientId);
            } else {
                // No more assistants, clear the primary assistant
                client.setVapiAssistantId(null);
                log.info("Cleared primary assistant for client {}", clientId);
            }
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Find all clients assigned to a specific Vapi assistant
     * @param assistantId The Vapi assistant ID
     * @return List of client DTOs assigned to the assistant
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findClientsByAssistantId(String assistantId) {
        // Get clients with this ID as primary assistant
        List<Client> primaryClients = clientRepository.findByVapiAssistantId(assistantId);

        // Get clients that have this assistant in their collection
        VapiAssistant assistant = vapiAssistantRepository.findById(assistantId).orElse(null);
        List<Client> secondaryClients = assistant != null ? 
                clientRepository.findByVapiAssistantsContaining(assistant) :
                new ArrayList<>();

        // Combine the lists, removing duplicates
        Set<Client> allClients = new HashSet<>();
        allClients.addAll(primaryClients);
        allClients.addAll(secondaryClients);

        return allClients.stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all Vapi assistants assigned to a client
     * @param clientId The client ID
     * @return List of VapiAssistantDTO objects assigned to the client
     */
    @Override
    @Transactional(readOnly = true)
    public List<VapiAssistantDTO> getAssignedVapiAssistants(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Get all assistants from the client's entity relationship
        List<VapiAssistantDTO> assistants = client.getVapiAssistants().stream()
                .map(vapiAssistantMapper::toDto)
                .collect(Collectors.toList());

        // If there are no assistants in the collection but the client has a primary assistant ID,
        // try to find that assistant and add it (backward compatibility)
        if (assistants.isEmpty() && client.getVapiAssistantId() != null) {
            VapiAssistant primaryAssistant = vapiAssistantRepository.findById(client.getVapiAssistantId())
                    .orElse(null);
            if (primaryAssistant != null) {
                assistants.add(vapiAssistantMapper.toDto(primaryAssistant));
            }
        }

        return assistants;
    }

    /**
     * Generate a unique API key for new clients
     * @return A random 32-character API key
     */
    /**
     * Update or save a local copy of a Vapi assistant from the Vapi API
     * @param assistantId The assistant ID to update
     * @return The updated VapiAssistant entity
     */
    private VapiAssistant updateLocalAssistantFromVapi(String assistantId) {
        try {
            // Check if we already have this assistant
            VapiAssistant existingAssistant = vapiAssistantRepository.findById(assistantId).orElse(null);

            // If not found or it's been a while since updated, fetch from Vapi API
            if (existingAssistant == null) {
                // Get from Vapi API
                VapiAssistantDTO assistantDTO = null;
                try {
                    // Get all assistants and find the one with matching ID
                    VapiListAssistantsResponse response = vapiAgentService.getAllAssistants();
                    if (response != null && response.getAssistants() != null) {
                        assistantDTO = response.getAssistants().stream()
                                .filter(a -> assistantId.equals(a.getAssistantId()))
                                .findFirst()
                                .orElse(null);
                    }
                } catch (Exception e) {
                    // Log the error but continue, we'll create a placeholder assistant
                    log.error("Error fetching assistant from Vapi API: {}", e.getMessage());
                }

                if (assistantDTO != null) {
                    // Convert to entity and save
                    VapiAssistant assistant = vapiAssistantMapper.toEntity(assistantDTO);
                    // Important: preserve any existing client relationship when updating
                    if (existingAssistant != null && existingAssistant.getClient() != null) {
                        assistant.setClient(existingAssistant.getClient());
                    }
                    return vapiAssistantRepository.save(assistant);
                } else {
                    // Create a placeholder with just the ID
                    VapiAssistant placeholder = new VapiAssistant();
                    placeholder.setAssistantId(assistantId);
                    placeholder.setName("Unknown Assistant");
                    placeholder.setStatus("unknown");
                    return vapiAssistantRepository.save(placeholder);
                }
            }

            return existingAssistant;
        } catch (Exception e) {
            log.error("Error updating local assistant data: {}", e.getMessage());
            return null;
        }
    }

    private String generateUniqueApiKey() {
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        String apiKey = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Check if the API key already exists and regenerate if needed
        while (clientRepository.findByApiKey(apiKey).isPresent()) {
            new java.security.SecureRandom().nextBytes(bytes);
            apiKey = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }

        return apiKey;
    }

    /**
     * Assign an ElevenLabs assistant to a client
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO assignElevenLabsAssistant(Long clientId, String elevenLabsAssistantId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Get or create the assistant entity
        ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(elevenLabsAssistantId)
                .orElseGet(() -> updateLocalElevenLabsAssistantFromApi(elevenLabsAssistantId));

        if (assistant != null) {
            // Set the client reference on the assistant
            assistant.setClient(client);
            
            // Save the assistant first to ensure it exists
            elevenLabsAssistantRepository.save(assistant);
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Unassign (remove) the ElevenLabs assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO unassignElevenLabsAssistant(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Find and unassign any ElevenLabs assistants
        List<ElevenLabsAssistant> assistants = elevenLabsAssistantRepository.findByClientId(clientId);
        for (ElevenLabsAssistant assistant : assistants) {
            assistant.setClient(null);
            elevenLabsAssistantRepository.save(assistant);
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Add an ElevenLabs assistant to a client's list of assistants
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO addElevenLabsAssistant(Long clientId, String elevenLabsAssistantId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Get or create the assistant entity
        ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(elevenLabsAssistantId)
                .orElseGet(() -> updateLocalElevenLabsAssistantFromApi(elevenLabsAssistantId));

        if (assistant != null) {
            // Check if this assistant is already associated with this client
            if (assistant.getClient() == null || !assistant.getClient().getId().equals(clientId)) {
                // Set the client reference on the assistant
                assistant.setClient(client);
                
                // Save the assistant
                elevenLabsAssistantRepository.save(assistant);
            }
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Remove an ElevenLabs assistant from a client's list of assistants
     * @param clientId Client ID
     * @param elevenLabsAssistantId ElevenLabs assistant ID
     * @return Updated client DTO
     */
    @Override
    @Transactional
    public ClientDTO removeElevenLabsAssistant(Long clientId, String elevenLabsAssistantId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Find the assistant and remove the client association
        ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(elevenLabsAssistantId)
                .orElse(null);

        if (assistant != null && assistant.getClient() != null && assistant.getClient().getId().equals(clientId)) {
            assistant.setClient(null);
            elevenLabsAssistantRepository.save(assistant);
        }

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toDto(updatedClient);
    }

    /**
     * Find all clients assigned to a specific ElevenLabs assistant
     * @param assistantId The ElevenLabs assistant ID
     * @return List of client DTOs assigned to the assistant
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClientDTO> findClientsByElevenLabsAssistantId(String assistantId) {
        ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(assistantId)
                .orElse(null);

        if (assistant == null || assistant.getClient() == null) {
            return new ArrayList<>();
        }

        return List.of(clientMapper.toDto(assistant.getClient()));
    }

    /**
     * Get all ElevenLabs assistants assigned to a client
     * @param clientId The client ID
     * @return List of ElevenLabsAssistantDTO objects assigned to the client
     */
    @Override
    @Transactional(readOnly = true)
    public List<ElevenLabsAssistantDTO> getAssignedElevenLabsAssistants(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + clientId));

        // Get all assistants from the client's entity relationship
        List<ElevenLabsAssistantDTO> assistants = elevenLabsAssistantRepository.findByClientId(clientId).stream()
                .map(elevenLabsAssistantMapper::toDto)
                .collect(Collectors.toList());

        return assistants;
    }

    /**
     * Update or save a local copy of an ElevenLabs assistant from the ElevenLabs API
     * @param assistantId The assistant ID to update
     * @return The updated ElevenLabsAssistant entity
     */
    private ElevenLabsAssistant updateLocalElevenLabsAssistantFromApi(String assistantId) {
        try {
            // Check if we already have this assistant
            ElevenLabsAssistant existingAssistant = elevenLabsAssistantRepository.findById(assistantId).orElse(null);

            // If not found or it's been a while since updated, fetch from ElevenLabs API
            if (existingAssistant == null) {
                // Get from ElevenLabs API
                ElevenLabsAssistantDTO assistantDTO = null;
                try {
                    assistantDTO = elevenLabsAssistantService.getAssistant(assistantId);
                } catch (Exception e) {
                    // Log the error but continue, we'll create a placeholder assistant
                    log.error("Error fetching assistant from ElevenLabs API: {}", e.getMessage());
                }

                if (assistantDTO != null) {
                    // Convert to entity and save
                    ElevenLabsAssistant newAssistant = elevenLabsAssistantMapper.toEntity(assistantDTO);
                    return elevenLabsAssistantRepository.save(newAssistant);
                } else {
                    // Create a placeholder assistant if we can't fetch from API
                    ElevenLabsAssistant placeholderAssistant = ElevenLabsAssistant.builder()
                            .assistantId(assistantId)
                            .name("Unknown ElevenLabs Assistant")
                            .build();
                    return elevenLabsAssistantRepository.save(placeholderAssistant);
                }
            }

            return existingAssistant;
        } catch (Exception e) {
            log.error("Error updating local ElevenLabs assistant from API: {}", e.getMessage());
            // Create a placeholder assistant as fallback
            ElevenLabsAssistant placeholderAssistant = ElevenLabsAssistant.builder()
                    .assistantId(assistantId)
                    .name("Unknown ElevenLabs Assistant")
                    .build();
            return elevenLabsAssistantRepository.save(placeholderAssistant);
        }
    }
}
