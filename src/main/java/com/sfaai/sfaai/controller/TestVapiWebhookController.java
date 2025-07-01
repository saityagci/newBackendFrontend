package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiWebhookPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for debugging Vapi webhook issues
 * Uses String as request body to log the raw payload before processing
 */
@RestController
@RequestMapping("/api/webhooks/vapi/test")
@RequiredArgsConstructor
@Slf4j
public class TestVapiWebhookController {

    private final ObjectMapper objectMapper;

    @PostMapping("/call-logs")
    public ResponseEntity<?> receiveCallLog(@RequestBody String raw) {
        log.info("RAW JSON PAYLOAD: {}", raw);

        try {
            // Convert to DTO manually after logging the raw string
            VapiWebhookPayloadDTO dto = objectMapper.readValue(raw, VapiWebhookPayloadDTO.class);

            // Log parsed DTO information
            log.info("DTO properties: {}", dto.getProperties());
            log.info("DTO message: {}", dto.getMessage());
            log.info("DTO getAnyRecordingUrl(): {}", dto.getAnyRecordingUrl());
            log.info("DTO getAudioUrl(): {}", dto.getAudioUrl());

            // Return a simple success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recordingUrl", dto.getAnyRecordingUrl());
            response.put("messageArtifactUrl", dto.getAudioUrl());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error processing webhook payload", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
