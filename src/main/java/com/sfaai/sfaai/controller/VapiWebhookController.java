package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiCallLogDTO;
import com.sfaai.sfaai.dto.VapiWebhookPayloadDTO;
import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.mapper.VapiWebhookMapper;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.AudioStorageService;
import com.sfaai.sfaai.service.VoiceLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/vapi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vapi Webhooks", description = "Endpoints for receiving webhooks from Vapi")
public class VapiWebhookController {

    private final VapiWebhookMapper webhookMapper;
    private final VoiceLogService voiceLogService;
    private final AudioStorageService audioStorageService;
    private final ClientRepository clientRepository;
    private final AgentRepository agentRepository;
    private final VapiAssistantRepository vapiAssistantRepository;

    @Operation(
        summary = "Receive call log webhook from Vapi",
        description = "Endpoint for receiving call log webhooks from Vapi. Accepts any JSON payload structure.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class),
                examples = {
                    @ExampleObject(
                        name = "Sample Vapi webhook payload",
                        value = "{\n" +
                               "  \"call\": {\n" +
                               "    \"id\": \"call_123456789\",\n" +
                               "    \"status\": \"completed\",\n" +
                               "    \"startTime\": 1687452378,\n" +
                               "    \"endTime\": 1687452498,\n" +
                               "    \"recordingUrl\": \"https://storage.vapi.ai/recordings/call_123456789.mp3\"\n" +
                               "  },\n" +
                               "  \"assistant\": {\n" +
                               "    \"id\": \"asst_abcdef123456\",\n" +
                               "    \"name\": \"Customer Service Bot\"\n" +
                               "  },\n" +
                               "  \"transcript\": {\n" +
                               "    \"text\": \"User: Hello\\nAssistant: Hi there, how can I help you today?\"\n" +
                               "  },\n" +
                               "  \"messages\": [\n" +
                               "    {\n" +
                               "      \"role\": \"user\",\n" +
                               "      \"content\": \"Hello\",\n" +
                               "      \"timestamp\": 1687452380\n" +
                               "    },\n" +
                               "    {\n" +
                               "      \"role\": \"assistant\",\n" +
                               "      \"content\": \"Hi there, how can I help you today?\",\n" +
                               "      \"timestamp\": 1687452385\n" +
                               "    }\n" +
                               "  ]\n" +
                               "}\n",
                        summary = "Standard Vapi call log webhook payload"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Webhook processed successfully",
                content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid webhook payload"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Required resources not found (client, agent, or assistant)"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        }
    )
    @PostMapping(value = "/call-logs", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> receiveCallLogWebhook(HttpServletRequest request) throws IOException {
        log.info("Received Vapi call log webhook");

        // Read the raw JSON string from the request before mapping to DTO
        String rawJson = new BufferedReader(new InputStreamReader(request.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        log.info("RAW JSON WEBHOOK PAYLOAD: {}", rawJson);

        // Parse to DTO using ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        VapiWebhookPayloadDTO payload = objectMapper.readValue(rawJson, VapiWebhookPayloadDTO.class);

        log.info("Full webhook payload properties: {}", payload.getProperties());
        // --- ADD THIS FOR DEBUGGING ---
        // Print all top-level property keys (optional)
        log.debug("Webhook payload received with keys: {}",
                payload != null ? String.join(", ", payload.getProperties().keySet()) : "null");
        // Print the audio URL from the payload DTO
        log.info("Recording URL from DTO: {}", payload.getAnyRecordingUrl());

        // Log detailed DTO information for debugging
        log.info("DTO properties: {}", payload.getProperties());
        log.info("DTO message: {}", payload.getMessage());
        log.info("DTO getAnyRecordingUrl(): {}", payload.getAnyRecordingUrl());
        log.info("DTO getAudioUrl(): {}", payload.getAudioUrl());
        // --- END DEBUGGING ---
        try {
            // Parse the webhook payload
            VapiCallLogDTO callLog = webhookMapper.parseWebhookPayload(payload);
            if (callLog == null) {
                log.warn("Failed to parse webhook payload");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Failed to parse webhook payload");
                return ResponseEntity.badRequest().body(response);
            }

            // Log only essential info, not the entire payload
            log.info("Parsed call log with ID: {}, assistant ID: {}, status: {}, audio URL: {}",
                    callLog.getCallId(), 
                    callLog.getAssistantId(),
                    callLog.getStatus());
                    callLog.getAudioUrl();

            // If the call has no ID, generate a response with an error
            if (callLog.getCallId() == null) {
                log.warn("Call log has no ID, cannot process");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Call ID not found in webhook payload");
                return ResponseEntity.badRequest().body(response);
            }

            // Find the assistant from the call log
            String assistantId = callLog.getAssistantId();
            if (assistantId == null) {
                log.warn("Call log has no assistant ID, cannot process");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Assistant ID not found in webhook payload");
                return ResponseEntity.badRequest().body(response);
            }

            // Lookup the assistant in the database
            VapiAssistant assistant = vapiAssistantRepository.findById(assistantId).orElse(null);
            if (assistant == null) {
                log.warn("Assistant with ID {} not found in database", assistantId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Assistant not found in database");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Find the client associated with this assistant
            Client client = assistant.getClient();
            if (client == null) {
                // Try to find the client by using the legacy vapiAssistantId field
                List<Client> clients = clientRepository.findByVapiAssistantId(assistantId);
                if (!clients.isEmpty()) {
                    client = clients.get(0);
                } else {
                    log.warn("No client found for assistant ID: {}", assistantId);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("error", "No client found for this assistant");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }

            // Find or create an agent for this client
            Long agentId = null;
            List<com.sfaai.sfaai.entity.Agent> agents = agentRepository.findByClientIdAndStatus(
                    client.getId(), com.sfaai.sfaai.entity.Agent.AgentStatus.ACTIVE);

            if (!agents.isEmpty()) {
                // Use the first active agent
                agentId = agents.get(0).getId();
            } else {
                log.warn("No active agent found for client ID: {}", client.getId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "No active agent found for the client");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Process the audio URL if present
            String audioUrl = callLog.getAudioUrl();
            if (audioUrl != null && !audioUrl.isEmpty()) {
                // Download and store the audio file locally
                try {
                    String storedPath = audioStorageService.storeAudioFromUrl(audioUrl, callLog.getCallId());
                    String publicUrl = audioStorageService.getPublicUrl(storedPath);

                    // Update the URL in the call log
                    if (publicUrl != null && !publicUrl.isEmpty()) {
                        log.info("Updated audio URL from {} to {}", audioUrl, publicUrl);
                        callLog.setAudioUrl(publicUrl);
                    }
                } catch (Exception e) {
                    log.error("Failed to process audio URL: {}", audioUrl, e);
                    // Continue with the original URL
                }
            } else {
                log.warn("No audio URL found in call log with ID: {}", callLog.getCallId());
                // We could attempt to extract from other parts of the payload here if needed
            }

            // Convert to VoiceLogCreateDTO
            VoiceLogCreateDTO createDTO = webhookMapper.toVoiceLogCreateDTO(callLog, client.getId(), agentId);

            // Check if this is a new record or an update to an existing one
            VoiceLogDTO existingLog = null;
            if (callLog.getCallId() != null) {
                existingLog = voiceLogService.getVoiceLogByExternalCallId(callLog.getCallId());
            }

            // Save to database (createVoiceLog will handle both creation and updates)
            VoiceLogDTO savedLog = voiceLogService.createVoiceLog(createDTO);

            // Return appropriate status code and response based on whether this was an update or creation
            HttpStatus status;
            String message;

            if (existingLog != null) {
                log.info("Updated existing voice log with ID: {} for external call ID: {}", 
                         savedLog.getId(), callLog.getCallId());
                status = HttpStatus.OK;
                message = "Webhook processed successfully (updated existing record)";
            } else {
                log.info("Created new voice log with ID: {} for external call ID: {}", 
                         savedLog.getId(), callLog.getCallId());
                status = HttpStatus.CREATED;
                message = "Webhook processed successfully (created new record)";
            }

            // Build the response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            response.put("voiceLogId", savedLog.getId());
            response.put("externalCallId", callLog.getCallId());
            response.put("isUpdate", existingLog != null);

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("Error processing Vapi webhook", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
