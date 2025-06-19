package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.config.VapiConfig;
import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiCreateAssistantRequest;
import com.sfaai.sfaai.dto.VapiCreateAssistantResponse;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.exception.ExternalApiException;
import com.sfaai.sfaai.service.VapiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of Vapi agent service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VapiAgentServiceImpl implements VapiAgentService {

    private final RestTemplate restTemplate;
    private final VapiConfig vapiConfig;

    /**
     * Gets all assistants from Vapi API
     * @return List of assistants response
     */
    @Override
    public VapiListAssistantsResponse getAllAssistants() {
        log.info("Fetching all Vapi assistants");

        // Get API key and log a masked version
        String apiKey = vapiConfig.getApiKey();
        log.debug("Using Vapi API key: {}...", apiKey.substring(0, 5) + "*****");
        log.debug("Using Vapi API URL: {}", vapiConfig.getApiUrl());

        // Set up headers with API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Call Vapi API
            String url = vapiConfig.getApiUrl() + "/assistant";
            log.debug("Making GET request to: {}", url);

            try {
                // Make the request
                ResponseEntity<VapiAssistantDTO[]> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        VapiAssistantDTO[].class);

                List<VapiAssistantDTO> assistants = response.getBody() != null ?
                        Arrays.asList(response.getBody()) : Collections.emptyList();

                log.info("Successfully fetched {} Vapi assistants", assistants.size());

                // Create response DTO
                VapiListAssistantsResponse result = new VapiListAssistantsResponse();
                result.setAssistants(assistants);
                result.setTotal(assistants.size());
                result.setPage(1);
                result.setLimit(assistants.size());

                return result;

            } catch (HttpClientErrorException.BadRequest ex) {
                String errorBody = ex.getResponseBodyAsString();
                log.error("Vapi API validation error: {}", errorBody);
                throw new ExternalApiException("Vapi API rejected the request: " + errorBody, "Vapi", "VALIDATION_ERROR", ex);
            } catch (HttpClientErrorException.Unauthorized ex) {
                throw new ExternalApiException("Invalid Vapi API key or authentication failure", "Vapi", "AUTH_ERROR", ex);
            } catch (HttpServerErrorException ex) {
                throw new ExternalApiException("Vapi API server error: " + ex.getMessage(), "Vapi", "SERVER_ERROR", ex);
            } catch (ResourceAccessException ex) {
                throw new ExternalApiException("Could not connect to Vapi API: " + ex.getMessage(), "Vapi", "CONNECTION_ERROR", ex);
            }

        } catch (Exception e) {
            if (!(e instanceof ExternalApiException)) {
                log.error("Error fetching Vapi assistants: {}", e.getMessage(), e);
                throw new ExternalApiException("Failed to fetch Vapi assistants: " + e.getMessage(), "Vapi", "UNKNOWN_ERROR", e);
            }
            throw e;
        }
    }

    @Override
    public VapiCreateAssistantResponse createAssistant(VapiCreateAssistantRequest request) {
        log.info("Creating new Vapi assistant");

        // Get API key and log a masked version
        String apiKey = vapiConfig.getApiKey();
        log.debug("Using Vapi API key: {}...", apiKey.substring(0, 5) + "*****");
        log.debug("Using Vapi API URL: {}", vapiConfig.getApiUrl());

        // Set up headers with API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<VapiCreateAssistantRequest> entity = new HttpEntity<>(request, headers);

        try {
            // Call Vapi API
            log.debug("Calling Vapi API with request: {}", request);
            VapiCreateAssistantResponse response;

            try {
                String url = vapiConfig.getApiUrl() + "/assistant";
                log.debug("Making POST request to: {}", url);
                response = restTemplate.postForObject(
                        url,
                        entity,
                        VapiCreateAssistantResponse.class);
            } catch (HttpClientErrorException.BadRequest ex) {
                // Handle 400 errors from Vapi API
                String errorBody = ex.getResponseBodyAsString();
                log.error("Vapi API validation error: {}", errorBody);
                throw new ExternalApiException("Vapi API rejected the request: " + errorBody, "Vapi", "VALIDATION_ERROR", ex);
            } catch (HttpClientErrorException.Unauthorized ex) {
                // Handle 401 errors from Vapi API
                throw new ExternalApiException("Invalid Vapi API key or authentication failure", "Vapi", "AUTH_ERROR", ex);
            } catch (HttpServerErrorException ex) {
                // Handle 5xx errors from Vapi API
                throw new ExternalApiException("Vapi API server error: " + ex.getMessage(), "Vapi", "SERVER_ERROR", ex);
            } catch (ResourceAccessException ex) {
                // Handle network/connection errors
                throw new ExternalApiException("Could not connect to Vapi API: " + ex.getMessage(), "Vapi", "CONNECTION_ERROR", ex);
            }

            if (response == null || response.getAssistantId() == null) {
                throw new ExternalApiException("Failed to create Vapi assistant: null or invalid response", "Vapi", "INVALID_RESPONSE");
            }

            log.info("Vapi assistant created successfully with ID: {}", response.getAssistantId());
            return response;

        } catch (Exception e) {
            if (!(e instanceof ExternalApiException)) {
                log.error("Error creating Vapi assistant: {}", e.getMessage(), e);
                throw new ExternalApiException("Failed to create Vapi assistant: " + e.getMessage(), "Vapi", "UNKNOWN_ERROR", e);
            }
            throw e;
        }
    }
}
