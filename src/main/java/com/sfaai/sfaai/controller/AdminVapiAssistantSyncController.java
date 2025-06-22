package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.service.VapiAssistantSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for manually triggering Vapi assistant synchronization
 */
@RestController
@RequestMapping("/api/admin/vapi-sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vapi Sync", description = "Endpoints for managing Vapi assistant synchronization")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVapiAssistantSyncController {

    private final VapiAssistantSyncService vapiAssistantSyncService;

    /**
     * Manually trigger synchronization of all Vapi assistants
     * @return Sync result
     */
    @Operation(
        summary = "Synchronize all Vapi assistants", 
        description = "Manually triggers synchronization of all Vapi assistants"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronization triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Error during synchronization")
    })
    @PostMapping("/assistants")
    public ResponseEntity<Map<String, Object>> syncAllAssistants() {
        log.info("Manual trigger: Synchronizing all Vapi assistants");

        Map<String, Object> response = new HashMap<>();
        try {
            int syncCount = vapiAssistantSyncService.synchronizeAllAssistants();

            response.put("success", true);
            response.put("message", "Successfully synchronized assistants");
            response.put("syncCount", syncCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during manual sync of assistants: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error during synchronization: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Manually trigger synchronization of a specific Vapi assistant
     * @param assistantId Assistant ID to synchronize
     * @return Sync result
     */
    @Operation(
        summary = "Synchronize specific Vapi assistant", 
        description = "Manually triggers synchronization of a specific Vapi assistant"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronization triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Error during synchronization")
    })
    @PostMapping("/assistants/{assistantId}")
    public ResponseEntity<Map<String, Object>> syncAssistant(@PathVariable String assistantId) {
        log.info("Manual trigger: Synchronizing Vapi assistant with ID: {}", assistantId);

        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = vapiAssistantSyncService.synchronizeAssistant(assistantId);

            if (success) {
                response.put("success", true);
                response.put("message", "Successfully synchronized assistant " + assistantId);

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Assistant not found or could not be synchronized");

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error during manual sync of assistant {}: {}", assistantId, e.getMessage(), e);

            response.put("success", false);
            response.put("message", "Error during synchronization: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
