package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;
import com.sfaai.sfaai.service.ElevenLabsAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for ElevenLabs assistant management
 */
@RestController
@RequestMapping("/api/admin/elevenlabs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin ElevenLabs Management", description = "Endpoints for admin management of ElevenLabs assistants")
public class AdminElevenLabsController {

    private final ElevenLabsAssistantService elevenLabsAssistantService;

    /**
     * Get all ElevenLabs assistants from the API
     * @return List of all assistants
     */
    @Operation(
            summary = "Get all ElevenLabs assistants from API",
            description = "Retrieves all assistants from the ElevenLabs API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assistants retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ElevenLabsListAssistantsResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid API key"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - error communicating with ElevenLabs API")
    })
    @GetMapping("/assistants/api")
    public ResponseEntity<ElevenLabsListAssistantsResponse> getAllAssistantsFromApi() {
        return ResponseEntity.ok(elevenLabsAssistantService.getAllAssistantsFromApi());
    }

    /**
     * Get all ElevenLabs assistants from the database
     * @return List of assistant DTOs
     */
    @GetMapping("/assistants")
    @Operation(summary = "Get all ElevenLabs assistants", description = "Returns a list of all ElevenLabs assistants from the database")
    public ResponseEntity<List<ElevenLabsAssistantDTO>> getAllAssistants() {
        return ResponseEntity.ok(elevenLabsAssistantService.getAllAssistants());
    }

    /**
     * Get a single ElevenLabs assistant by ID
     * @param id Assistant ID
     * @return Assistant DTO
     */
    @GetMapping("/assistants/{id}")
    @Operation(summary = "Get an ElevenLabs assistant by ID", description = "Returns a specific ElevenLabs assistant by ID")
    public ResponseEntity<ElevenLabsAssistantDTO> getAssistant(@PathVariable String id) {
        ElevenLabsAssistantDTO assistant = elevenLabsAssistantService.getAssistant(id);
        if (assistant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assistant);
    }

    /**
     * Manually trigger synchronization of all ElevenLabs assistants
     * @return Sync result
     */
    @Operation(
            summary = "Synchronize all ElevenLabs assistants",
            description = "Manually triggers synchronization of all ElevenLabs assistants"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synchronization triggered successfully"),
            @ApiResponse(responseCode = "500", description = "Error during synchronization")
    })
    @PostMapping("/sync/assistants")
    public ResponseEntity<Map<String, Object>> syncAllAssistants() {
        try {
            int count = elevenLabsAssistantService.syncAllAssistants();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Successfully synchronized ElevenLabs assistants");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error synchronizing ElevenLabs assistants", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error synchronizing ElevenLabs assistants: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Manually trigger synchronization of all ElevenLabs assistants (verbose summary)
     * @return Sync result summary
     */
    @PostMapping("/sync/assistants/verbose")
    public ResponseEntity<Map<String, Object>> syncAllAssistantsVerbose() {
        log.info("Manual ElevenLabs assistant sync triggered via API");
        long start = System.currentTimeMillis();
        com.sfaai.sfaai.service.impl.ElevenLabsAssistantServiceImpl.SyncSummary summary =
                ((com.sfaai.sfaai.service.impl.ElevenLabsAssistantServiceImpl) elevenLabsAssistantService).syncAllAssistantsWithSummary();
        long duration = System.currentTimeMillis() - start;
        Map<String, Object> result = new HashMap<>();
        result.put("success", summary.errors == 0);
        result.put("message", summary.errors == 0 ? "Successfully synchronized ElevenLabs assistants" : "Errors occurred during sync");
        result.put("fetched", summary.fetched);
        result.put("updated", summary.updated);
        result.put("skipped", summary.skipped);
        result.put("errors", summary.errors);
        result.put("updatedIds", summary.updatedIds);
        result.put("skippedIds", summary.skippedIds);
        result.put("errorIds", summary.errorIds);
        result.put("durationMs", summary.durationMs);
        log.info("Manual sync result: {} fetched, {} updated, {} skipped, {} errors, {} ms", summary.fetched, summary.updated, summary.skipped, summary.errors, summary.durationMs);
        return ResponseEntity.ok(result);
    }
}
