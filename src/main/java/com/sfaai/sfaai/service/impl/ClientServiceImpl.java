package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.ClientMapper;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

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
     * Generate a unique API key for new clients
     * @return A random 32-character API key
     */
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
}
