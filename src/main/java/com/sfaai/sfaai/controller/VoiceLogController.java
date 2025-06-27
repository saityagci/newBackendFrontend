package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.dto.VoiceLogWebhookDTO;
import com.sfaai.sfaai.exception.WebhookException;
import com.sfaai.sfaai.mapper.VoiceLogWebhookMapper;
import com.sfaai.sfaai.service.VoiceLogService;
import com.sfaai.sfaai.util.WebhookSignatureVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voicelogs")
@RequiredArgsConstructor
@Tag(name = "Voice Logs", description = "Voice logs management API")
@Slf4j
public class VoiceLogController {
    private final VoiceLogService voiceLogService;
    private final WebhookSignatureVerifier signatureVerifier;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @Operation(
        summary = "Create voice log",
        responses = {
            @ApiResponse(responseCode = "201", description = "Voice log created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Transactional
    public ResponseEntity<VoiceLogDTO> createVoiceLog(@Valid @RequestBody VoiceLogCreateDTO dto) {
        log.info("Creating voice log for agent {} and client {}", dto.getAgentId(), dto.getClientId());
        VoiceLogDTO saved = voiceLogService.createVoiceLog(dto);
        log.debug("Created voice log with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Receive and process a webhook for voice logs
     * Uses an idempotent pattern to ensure duplicate webhooks don't create duplicate entries
     *
     * @param payload The webhook payload
     * @param signature The webhook signature for verification
     * @return The created or updated voice log
     */
    @PostMapping("/webhook")
    @Operation(
        summary = "Receive voice log webhook",
        responses = {
            @ApiResponse(responseCode = "201", description = "Webhook processed successfully (created new record)"),
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully (updated existing record)"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
            @ApiResponse(responseCode = "403", description = "Invalid webhook signature")
        }
    )
    @Transactional
    public ResponseEntity<VoiceLogDTO> receiveVoiceLogWebhook(
            @Valid @RequestBody VoiceLogWebhookDTO payload,
            @RequestHeader(value = "X-Webhook-Signature", required = true) String signature
    ) {
        log.info("Received webhook for provider: {}, callId: {}", 
                payload.getProvider(), 
                payload.getCallId() != null ? payload.getCallId() : "<none>");

        validateWebhookSignature(signature, payload);

        try {
            // Check if the call ID is provided
            if (payload.getCallId() == null || payload.getCallId().trim().isEmpty()) {
                log.warn("Webhook missing required call ID");
                throw new IllegalArgumentException("Call ID is required for webhook processing");
            }

            // Check if this is an update to an existing voice log
            boolean isUpdate = false;
            VoiceLogDTO existingLog = voiceLogService.getVoiceLogByExternalCallId(payload.getCallId());
            isUpdate = existingLog != null;

            if (isUpdate) {
                log.info("Processing update to existing voice log (ID: {}) for external call ID: {}", 
                        existingLog.getId(), payload.getCallId());
            } else {
                log.info("Processing new voice log for external call ID: {}", payload.getCallId());
            }

            // Convert webhook to create DTO and save/update using the idempotent pattern
            VoiceLogCreateDTO dto = VoiceLogWebhookMapper.toCreateDTO(payload);
            VoiceLogDTO saved = voiceLogService.createVoiceLog(dto);

            // Return appropriate status code and response based on whether this was an update or creation
            if (isUpdate) {
                log.info("Successfully processed webhook, updated voice log ID: {}", saved.getId());
                return ResponseEntity.status(HttpStatus.OK).body(saved);
            } else {
                log.info("Successfully processed webhook, created voice log ID: {}", saved.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            }
        } catch (Exception e) {
            log.error("Error processing webhook payload: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateWebhookSignature(String signature, VoiceLogWebhookDTO payload) {
        if (!signatureVerifier.verifySignature(signature, payload)) {
            log.warn("Invalid webhook signature received");
            throw new WebhookException("Invalid webhook signature");
        }
        log.debug("Webhook signature validated successfully");
    }
}