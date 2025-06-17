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

    @PostMapping("/webhook")
    @Operation(
        summary = "Receive voice log webhook",
        responses = {
            @ApiResponse(responseCode = "201", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
            @ApiResponse(responseCode = "403", description = "Invalid webhook signature")
        }
    )
    @Transactional
    public ResponseEntity<VoiceLogDTO> receiveVoiceLogWebhook(
            @Valid @RequestBody VoiceLogWebhookDTO payload,
            @RequestHeader(value = "X-Webhook-Signature", required = true) String signature
    ) {
        log.info("Received webhook for provider: {}", payload.getProvider());
        validateWebhookSignature(signature, payload);

        try {
            VoiceLogCreateDTO dto = VoiceLogWebhookMapper.toCreateDTO(payload);
            VoiceLogDTO saved = voiceLogService.createVoiceLog(dto);
            log.info("Successfully processed webhook, created voice log ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
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