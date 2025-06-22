package com.sfaai.sfaai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.util.ArrayList;
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
    private final com.sfaai.sfaai.util.FirstMessageFallbackAdapter firstMessageFallbackAdapter;

    /**
     * Creates a properly configured RestTemplate for Vapi API
     * This ensures snake_case JSON fields are mapped correctly
     */
    private RestTemplate createSnakeCaseRestTemplate() {
        RestTemplate template = new RestTemplate();

        // Create an ObjectMapper that converts snake_case to camelCase
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // Enable debug features
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Create a message converter with the custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        // Keep existing converters and add ours first for priority
        List<org.springframework.http.converter.HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(converter);
        messageConverters.addAll(template.getMessageConverters());
        template.setMessageConverters(messageConverters);

        return template;
    }

    /**
     * Gets all assistants from Vapi API
     * @return List of assistants response
     */
    @Override
    public VapiListAssistantsResponse getAllAssistants() {
        log.info("Fetching all Vapi assistants from API");

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
                                        // Log raw response first to check field names
                                        try {
                                            ResponseEntity<String> rawResponse = restTemplate.exchange(
                            url, HttpMethod.GET, entity, String.class);
                                            log.debug("Raw API response: {}", rawResponse.getBody());

                    // If we got a valid response, analyze the JSON structure
                    if (rawResponse.getBody() != null) {
                        String responseBody = rawResponse.getBody();
                        log.debug("Checking for 'first_message' in response: {}", 
                                responseBody.contains("first_message") ? "FOUND" : "NOT FOUND");

                        // Check for alternate field names the API might be using
                        log.debug("API FIELD DETECTION: 'firstMessage' exists: {}", responseBody.contains("firstMessage"));
                        log.debug("API FIELD DETECTION: 'initial_message' exists: {}", responseBody.contains("initial_message"));
                        log.debug("API FIELD DETECTION: 'default_message' exists: {}", responseBody.contains("default_message"));
                        log.debug("API FIELD DETECTION: 'greeting' exists: {}", responseBody.contains("greeting"));
                        log.debug("API FIELD DETECTION: 'welcome_message' exists: {}", responseBody.contains("welcome_message"));

                        // Log sample of the response to see its structure
                        log.debug("FIRST 500 CHARS OF RESPONSE: {}", 
                            responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

                        // Find the exact structure of the first_message field
                        int firstMessageIndex = responseBody.indexOf("first_message");
                        if (firstMessageIndex >= 0) {
                            int contextStart = Math.max(0, firstMessageIndex - 10);
                            int contextEnd = Math.min(responseBody.length(), firstMessageIndex + 50);
                            String context = responseBody.substring(contextStart, contextEnd);
                            log.debug("first_message context: {}", context);
                        }

                        log.debug("Checking for 'voice' in response: {}", 
                                responseBody.contains("voice") ? "FOUND" : "NOT FOUND");

                        if (responseBody.contains("voice")) {
                            log.debug("Checking for 'voice_id' in response: {}", 
                                    responseBody.contains("voice_id") ? "FOUND" : "NOT FOUND");

                            // Find the exact structure of the voice field
                            int voiceIndex = responseBody.indexOf("voice");
                            if (voiceIndex >= 0) {
                                int contextStart = Math.max(0, voiceIndex - 5);
                                int contextEnd = Math.min(responseBody.length(), voiceIndex + 100);
                                String context = responseBody.substring(contextStart, contextEnd);
                                log.debug("voice context: {}", context);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to get raw response for debugging: {}", e.getMessage());
                }

                // Use custom RestTemplate with snake_case support
                RestTemplate snakeCaseRestTemplate = createSnakeCaseRestTemplate();

                                        // First get raw JSON and manually convert to ensure we don't miss fields
                                        ResponseEntity<String> rawJsonResponse = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);

                                        String rawJson = rawJsonResponse.getBody();
                                        List<VapiAssistantDTO> manuallyMappedAssistants = new ArrayList<>();

                                        try {
                                            // Parse raw JSON with basic object mapper
                                            ObjectMapper mapper = new ObjectMapper();
                                            JsonNode rootNode = mapper.readTree(rawJson);

                                            // Log the full raw JSON for debugging
                                            log.debug("Full JSON structure: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));

                                            // Iterate through each assistant and manually map fields
                                            if (rootNode.isArray()) {
                        for (JsonNode node : rootNode) {
                            VapiAssistantDTO dto = new VapiAssistantDTO();

                            // Map basic fields
                            if (node.has("id")) dto.setAssistantId(node.get("id").asText());
                            if (node.has("name")) dto.setName(node.get("name").asText());
                            if (node.has("status")) dto.setStatus(node.get("status").asText());
                            // Explicitly map first_message field and check alternates
                            if (node.has("first_message") && !node.get("first_message").isNull()) {
                                String firstMessage = node.get("first_message").asText();
                                dto.setFirstMessage(firstMessage);
                                log.debug("Manually mapped first_message: {}", firstMessage);
                            } else if (node.has("firstMessage") && !node.get("firstMessage").isNull()) {
                                String firstMessage = node.get("firstMessage").asText();
                                dto.setFirstMessage(firstMessage);
                                log.debug("Manually mapped firstMessage (camelCase): {}", firstMessage);
                            } else {
                                log.debug("FIELD CHECK: Looking for alternate fields for assistant ID: {}", dto.getAssistantId());

                                // Check for alternate field names
                                if (node.has("initial_message") && !node.get("initial_message").isNull()) {
                                    String message = node.get("initial_message").asText();
                                    dto.setFirstMessage(message);
                                    log.debug("Found firstMessage as 'initial_message': {}", message);
                                } else if (node.has("default_message") && !node.get("default_message").isNull()) {
                                    String message = node.get("default_message").asText();
                                    dto.setFirstMessage(message);
                                    log.debug("Found firstMessage as 'default_message': {}", message);
                                } else if (node.has("greeting") && !node.get("greeting").isNull()) {
                                    String message = node.get("greeting").asText();
                                    dto.setFirstMessage(message);
                                    log.debug("Found firstMessage as 'greeting': {}", message);
                                } else if (node.has("welcome_message") && !node.get("welcome_message").isNull()) {
                                    String message = node.get("welcome_message").asText();
                                    dto.setFirstMessage(message);
                                    log.debug("Found firstMessage as 'welcome_message': {}", message);
                                } else {
                                    // Last resort: search for fields with "message" in the name
                                    node.fields().forEachRemaining(entry -> {
                                        String fieldName = entry.getKey();
                                        if (fieldName.toLowerCase().contains("message") && !entry.getValue().isNull()) {
                                            String message = entry.getValue().asText();
                                            log.debug("Found potential message field: {} with value: {}", 
                                                    fieldName, message);
                                            // Actually use this field value as a last resort
                                            if (dto.getFirstMessage() == null) {
                                                dto.setFirstMessage(message);
                                                log.debug("Applied message from field '{}': {}", fieldName, message);
                                            }
                                        }
                                    });
                                }
                            }

                            // Map voice info
                            if (node.has("voice") && !node.get("voice").isNull()) {
                                JsonNode voiceNode = node.get("voice");
                                VapiAssistantDTO.VoiceInfo voiceInfo = new VapiAssistantDTO.VoiceInfo();

                                if (voiceNode.has("provider")) {
                                    voiceInfo.setProvider(voiceNode.get("provider").asText());
                                }

                                if (voiceNode.has("voice_id")) {
                                    voiceInfo.setVoiceId(voiceNode.get("voice_id").asText());
                                }

                                dto.setVoice(voiceInfo);
                                log.debug("Manually mapped voice: provider={}, voice_id={}", 
                                        voiceInfo.getProvider(), voiceInfo.getVoiceId());
                            } else {
                                log.debug("No voice information found in the response for assistant ID: {}", dto.getAssistantId());
                            }

                            // Add to list
                            manuallyMappedAssistants.add(dto);
                        }
                                            }

                                            log.debug("Manually mapped {} assistants", manuallyMappedAssistants.size());
                                        } catch (Exception e) {
                                            log.error("Error manually mapping assistants: {}", e.getMessage(), e);
                                        }

                                        // Also try the automatic mapping as a backup
                ResponseEntity<VapiAssistantDTO[]> response = snakeCaseRestTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        VapiAssistantDTO[].class);

                log.debug("API response status: {}", response.getStatusCode());
                log.debug("API response headers: {}", response.getHeaders());

                if (response.getBody() == null) {
                    log.warn("API returned null response body");
                }

                                        List<VapiAssistantDTO> assistants;

                                        // Use manually mapped assistants if available, otherwise use automatic mapping
                                        if (!manuallyMappedAssistants.isEmpty()) {
                                            log.info("Using {} manually mapped assistants", manuallyMappedAssistants.size());
                                            assistants = manuallyMappedAssistants;

                                            // Debug each assistant to check for null fields
                                            for (VapiAssistantDTO assistant : assistants) {
                                                log.debug("Assistant debug check - ID: {}, firstMessage: {}, voice: {}", 
                                                    assistant.getAssistantId(), 
                                                    assistant.getFirstMessage(), 
                                                    assistant.getVoice());
                                            }
                                        } else {
                                            log.info("Using automatically mapped assistants");
                                            assistants = response.getBody() != null ?
                            Arrays.asList(response.getBody()) : Collections.emptyList();
                                        }

                log.info("Successfully fetched {} Vapi assistants", assistants.size());

                // Log details of first few assistants for debugging
                if (!assistants.isEmpty()) {
                    int logCount = Math.min(2, assistants.size());
                    for (int i = 0; i < logCount; i++) {
                        VapiAssistantDTO assistant = assistants.get(i);
                        log.debug("Assistant #{}: ID={}, Name={}, Status={}", 
                            i+1, assistant.getAssistantId(), assistant.getName(), assistant.getStatus());

                        // Log the critical fields we're having trouble with
                        log.debug("  - firstMessage: {}", assistant.getFirstMessage());

                        if (assistant.getVoice() != null) {
                            log.debug("  - voice provider: {}", assistant.getVoice().getProvider());
                            log.debug("  - voice ID: {}", assistant.getVoice().getVoiceId());
                        } else {
                            log.debug("  - voice object is null");
                        }

                        // Additional fields
                        if (assistant.getModel() != null) {
                            log.debug("  - model: {}/{}", 
                                assistant.getModel().getProvider(), 
                                assistant.getModel().getModel());
                        }
                    }
                }

                // Apply fallback messages to any assistants with null firstMessage
                if (assistants != null) {
                    for (VapiAssistantDTO assistant : assistants) {
                        if (assistant.getFirstMessage() == null) {
                            log.debug("Assistant {} has null firstMessage, applying fallback", assistant.getAssistantId());
                            firstMessageFallbackAdapter.applyFallbackMessage(assistant);
                        }
                    }
                }

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

                // Use custom RestTemplate with snake_case support
                RestTemplate snakeCaseRestTemplate = createSnakeCaseRestTemplate();

                response = snakeCaseRestTemplate.postForObject(
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
