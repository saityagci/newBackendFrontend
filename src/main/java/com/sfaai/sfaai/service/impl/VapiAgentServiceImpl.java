package com.sfaai.sfaai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiAgentRequestDTO;
import com.sfaai.sfaai.dto.VapiAgentResponseDTO;
import com.sfaai.sfaai.dto.VapiExternalRequestDTO;
import com.sfaai.sfaai.dto.VapiExternalResponseDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAgent;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VapiAgentRepository;
import com.sfaai.sfaai.service.VapiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Vapi agent service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VapiAgentServiceImpl implements VapiAgentService {

    private final VapiAgentRepository vapiAgentRepository;
    private final ClientRepository clientRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${vapi.api.url}")
    private String vapiApiUrl;

    @Value("${vapi.api.key}")
    private String vapiApiKey;

    @Override
    @Transactional
    public VapiAgentResponseDTO createVapiAgent(VapiAgentRequestDTO requestDTO) {
        log.info("Creating new Vapi agent: {}", requestDTO.getName());

        // Find client
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + requestDTO.getClientId()));

        // Prepare request for Vapi API
        VapiExternalRequestDTO externalRequest = VapiExternalRequestDTO.builder()
                .name(requestDTO.getName())
                .greeting(requestDTO.getGreeting())
                .language(requestDTO.getLanguage())
                .public_agent(false) // Default value, can be made configurable
                .build();

        // Set up headers with API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + vapiApiKey);

        HttpEntity<VapiExternalRequestDTO> entity = new HttpEntity<>(externalRequest, headers);

        try {
            // Call Vapi API
            log.debug("Calling Vapi API with request: {}", externalRequest);
            VapiExternalResponseDTO vapiResponse = restTemplate.postForObject(
                    vapiApiUrl + "/agents", 
                    entity, 
                    VapiExternalResponseDTO.class);

            if (vapiResponse == null || vapiResponse.getId() == null) {
                throw new RuntimeException("Failed to create Vapi agent: null or invalid response");
            }

            log.info("Vapi agent created successfully with ID: {}", vapiResponse.getId());

            // Convert Vapi response to JSON for storage
            String vapiDetailsJson = objectMapper.writeValueAsString(vapiResponse);

            // Create and save local entity
            VapiAgent vapiAgent = VapiAgent.builder()
                    .vapiAgentId(vapiResponse.getId())
                    .name(vapiResponse.getName())
                    .greeting(vapiResponse.getGreeting())
                    .language(vapiResponse.getLanguage())
                    .status(vapiResponse.getStatus())
                    .voiceId(vapiResponse.getVoice_id())
                    .publicAgent(vapiResponse.getPublic_agent())
                    .vapiDetails(vapiDetailsJson)
                    .client(client)
                    .build();

            VapiAgent savedAgent = vapiAgentRepository.save(vapiAgent);

            // Convert to response DTO
            return mapToResponseDTO(savedAgent, vapiResponse);

        } catch (Exception e) {
            log.error("Error creating Vapi agent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Vapi agent: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VapiAgentResponseDTO getVapiAgent(Long id) {
        log.info("Getting Vapi agent with ID: {}", id);

        VapiAgent vapiAgent = vapiAgentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vapi agent not found with ID: " + id));

        try {
            // Parse stored Vapi details
            VapiExternalResponseDTO vapiDetails = objectMapper.readValue(
                    vapiAgent.getVapiDetails(), 
                    VapiExternalResponseDTO.class);

            return mapToResponseDTO(vapiAgent, vapiDetails);
        } catch (Exception e) {
            log.error("Error parsing Vapi details: {}", e.getMessage(), e);
            // Return basic response without Vapi details if parsing fails
            return mapToResponseDTO(vapiAgent, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VapiAgentResponseDTO getVapiAgentByExternalId(String vapiAgentId) {
        log.info("Getting Vapi agent with external ID: {}", vapiAgentId);

        VapiAgent vapiAgent = vapiAgentRepository.findByVapiAgentId(vapiAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Vapi agent not found with external ID: " + vapiAgentId));

        try {
            // Parse stored Vapi details
            VapiExternalResponseDTO vapiDetails = objectMapper.readValue(
                    vapiAgent.getVapiDetails(), 
                    VapiExternalResponseDTO.class);

            return mapToResponseDTO(vapiAgent, vapiDetails);
        } catch (Exception e) {
            log.error("Error parsing Vapi details: {}", e.getMessage(), e);
            // Return basic response without Vapi details if parsing fails
            return mapToResponseDTO(vapiAgent, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VapiAgentResponseDTO> getAllVapiAgents(Pageable pageable) {
        log.info("Getting all Vapi agents with pagination");

        return vapiAgentRepository.findAll(pageable)
                .map(agent -> {
                    try {
                        VapiExternalResponseDTO vapiDetails = objectMapper.readValue(
                                agent.getVapiDetails(), 
                                VapiExternalResponseDTO.class);
                        return mapToResponseDTO(agent, vapiDetails);
                    } catch (Exception e) {
                        log.error("Error parsing Vapi details: {}", e.getMessage(), e);
                        return mapToResponseDTO(agent, null);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<VapiAgentResponseDTO> getVapiAgentsByClientId(Long clientId) {
        log.info("Getting Vapi agents for client ID: {}", clientId);

        // Verify client exists
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with ID: " + clientId);
        }

        return vapiAgentRepository.findByClientId(clientId).stream()
                .map(agent -> {
                    try {
                        VapiExternalResponseDTO vapiDetails = objectMapper.readValue(
                                agent.getVapiDetails(), 
                                VapiExternalResponseDTO.class);
                        return mapToResponseDTO(agent, vapiDetails);
                    } catch (Exception e) {
                        log.error("Error parsing Vapi details: {}", e.getMessage(), e);
                        return mapToResponseDTO(agent, null);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteVapiAgent(Long id) {
        log.info("Deleting Vapi agent with ID: {}", id);

        VapiAgent vapiAgent = vapiAgentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vapi agent not found with ID: " + id));

        // Delete from Vapi API
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + vapiApiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            restTemplate.delete(vapiApiUrl + "/agents/" + vapiAgent.getVapiAgentId(), entity);
            log.info("Vapi agent deleted from Vapi API: {}", vapiAgent.getVapiAgentId());
        } catch (Exception e) {
            log.error("Error deleting Vapi agent from Vapi API: {}", e.getMessage(), e);
            // Continue with local deletion even if Vapi API call fails
        }

        // Delete from local database
        vapiAgentRepository.delete(vapiAgent);
        log.info("Vapi agent deleted from local database: {}", id);
    }

    /**
     * Map entity and Vapi response to response DTO
     */
    private VapiAgentResponseDTO mapToResponseDTO(VapiAgent entity, VapiExternalResponseDTO vapiDetails) {
        return VapiAgentResponseDTO.builder()
                .id(entity.getId())
                .vapiAgentId(entity.getVapiAgentId())
                .name(entity.getName())
                .greeting(entity.getGreeting())
                .language(entity.getLanguage())
                .clientId(entity.getClient().getId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .vapiDetails(vapiDetails) // Full Vapi response
                .build();
    }
}
