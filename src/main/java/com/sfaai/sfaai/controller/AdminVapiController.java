package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiCreateAssistantRequest;
import com.sfaai.sfaai.dto.VapiCreateAssistantResponse;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.SyncStatus;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.mapper.VapiAssistantMapper;
import com.sfaai.sfaai.repository.SyncStatusRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.ClientService;
import com.sfaai.sfaai.service.SecurityService;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidated admin controller for Vapi assistant operations, including:
 * - Retrieving assistant information
 * - Synchronizing assistants with the Vapi API
 * - Managing client-assistant relationships
 */
@RestController
@RequestMapping("/api/admin/vapi")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin Vapi Management", description = "Endpoints for admin management of Vapi assistants")
public class AdminVapiController {

    private final VapiAgentService vapiAgentService;
    private final VapiAssistantSyncService vapiAssistantSyncService;
    private final VapiAssistantRepository vapiAssistantRepository;
    private final VapiAssistantMapper vapiAssistantMapper;
    private final SecurityService securityService;
    private final ClientService clientService;
    private final SyncStatusRepository syncStatusRepository;

    //--------------------------------------------------------------------------
    // Assistant Management Endpoints
    //--------------------------------------------------------------------------

    /**
     * Create a new Vapi assistant
     * @param request Assistant creation data
     * @return Created assistant with HTTP 201 status
     */
    @Operation(
            summary = "Create a new Vapi assistant", 
            description = "Creates a new assistant via the Vapi API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", 
                    description = "Assistant created successfully",
                    content = @Content(schema = @Schema(implementation = VapiCreateAssistantResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid API key"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - error communicating with Vapi API")
    })
    @PostMapping("/assistants")
    public ResponseEntity<VapiCreateAssistantResponse> createAssistant(@Valid @RequestBody VapiCreateAssistantRequest request) {
        log.info("Creating new Vapi assistant with first message: {}", request.getFirstMessage());

        VapiCreateAssistantResponse response = vapiAgentService.createAssistant(request);

        log.info("Successfully created Vapi assistant with ID: {}", response.getAssistantId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all Vapi assistants from the Vapi API
     * @return List of all assistants
     */
    @Operation(
            summary = "Get all Vapi assistants from API", 
            description = "Retrieves all assistants from the Vapi API"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistants retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VapiListAssistantsResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid API key"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - error communicating with Vapi API")
    })
    @GetMapping("/assistants/api")
    public ResponseEntity<VapiListAssistantsResponse> getAllAssistantsFromApi() {
        log.info("Retrieving all Vapi assistants from API");

        VapiListAssistantsResponse response = vapiAgentService.getAllAssistants();

        log.info("Successfully retrieved {} Vapi assistants from API", 
                response.getAssistants() != null ? response.getAssistants().size() : 0);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all Vapi assistants from the database
     * @return List of assistant DTOs
     */
    @GetMapping("/assistants")
    @Operation(summary = "Get all Vapi assistants", description = "Returns a list of all Vapi assistants from the database")
    public ResponseEntity<List<VapiAssistantDTO>> getAllAssistants() {
        log.debug("REST request to get all Vapi assistants from database");

        List<VapiAssistant> assistants = vapiAssistantRepository.findAll();
        return ResponseEntity.ok(vapiAssistantMapper.toDtoList(assistants));
    }

    /**
     * Get a single Vapi assistant by ID
     * @param id Assistant ID
     * @return Assistant DTO
     */
    @GetMapping("/assistants/{id}")
    @Operation(summary = "Get a Vapi assistant by ID", description = "Returns a specific Vapi assistant by ID")
    public ResponseEntity<VapiAssistantDTO> getAssistant(@PathVariable String id) {
        log.debug("REST request to get Vapi assistant: {}", id);

        return vapiAssistantRepository.findById(id)
                .map(vapiAssistantMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Assistant {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistant not found");
                });
    }

    /**
     * Update an existing Vapi assistant
     * @param id Assistant ID
     * @param assistantDTO Assistant data to update
     * @return Updated assistant DTO
     */
    @PutMapping("/assistants/{id}")
    @Operation(summary = "Update a Vapi assistant", description = "Updates an existing Vapi assistant")
    public ResponseEntity<VapiAssistantDTO> updateAssistant(
            @PathVariable String id,
            @RequestBody VapiAssistantDTO assistantDTO) {
        log.debug("REST request to update Vapi assistant: {}", id);

        // Check if assistant exists
        if (!vapiAssistantRepository.existsById(id)) {
            log.warn("Assistant {} not found", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistant not found");
        }

        // Ensure the ID in the path matches the ID in the DTO
        if (!id.equals(assistantDTO.getAssistantId())) {
            log.error("ID mismatch: path ID {} != DTO ID {}", id, assistantDTO.getAssistantId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path must match ID in body");
        }

        // Convert DTO to entity, preserving existing fields not in the update
        VapiAssistant existingAssistant = vapiAssistantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistant not found"));

        // Update fields from DTO
        VapiAssistant updatedAssistant = vapiAssistantMapper.toEntity(assistantDTO);

        // Save the updated entity
        VapiAssistant savedAssistant = vapiAssistantRepository.save(updatedAssistant);
        log.info("Successfully updated Vapi assistant: {}", savedAssistant.getAssistantId());

        return ResponseEntity.ok(vapiAssistantMapper.toDto(savedAssistant));
    }

    /**
     * Delete a Vapi assistant
     * @param id Assistant ID to delete
     * @return No content response
     */
    @DeleteMapping("/assistants/{id}")
    @Operation(summary = "Delete a Vapi assistant", description = "Deletes a Vapi assistant from the database")
    public ResponseEntity<Void> deleteAssistant(@PathVariable String id) {
        log.debug("REST request to delete Vapi assistant: {}", id);

        if (!vapiAssistantRepository.existsById(id)) {
            log.warn("Assistant {} not found, cannot delete", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistant not found");
        }

        vapiAssistantRepository.deleteById(id);
        log.info("Successfully deleted Vapi assistant: {}", id);

        return ResponseEntity.noContent().build();
    }

    //--------------------------------------------------------------------------
    // Synchronization Endpoints
    //--------------------------------------------------------------------------

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
    @PostMapping("/sync/assistants")
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
    @PostMapping("/sync/assistants/{assistantId}")
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

    //--------------------------------------------------------------------------
    // Sync Status Endpoints
    //--------------------------------------------------------------------------

    /**
     * Get synchronization history
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Page of sync status entries
     */
    @Operation(
            summary = "Get synchronization history", 
            description = "Returns paginated history of synchronization operations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    })
    @GetMapping("/sync/history")
    public ResponseEntity<Page<SyncStatus>> getSyncHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Retrieving sync history page {} with size {}", page, size);

        Page<SyncStatus> history = syncStatusRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));

        return ResponseEntity.ok(history);
    }

    /**
     * Get synchronization summary statistics
     * @return Summary of sync operations
     */
    @Operation(
            summary = "Get synchronization summary", 
            description = "Returns summary statistics of synchronization operations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    })
    @GetMapping("/sync/summary")
    public ResponseEntity<Map<String, Object>> getSyncSummary() {

        log.info("Retrieving sync summary statistics");

        Map<String, Object> summary = new HashMap<>();

        // Get latest Vapi assistant sync
        SyncStatus latestVapiSync = syncStatusRepository
                .findTopBySyncTypeOrderByStartTimeDesc("VAPI_ASSISTANTS");

        if (latestVapiSync != null) {
            Map<String, Object> vapiSummary = new HashMap<>();
            vapiSummary.put("lastSyncTime", latestVapiSync.getStartTime());
            vapiSummary.put("lastSyncSuccess", latestVapiSync.isSuccess());
            vapiSummary.put("lastSyncItemsProcessed", latestVapiSync.getItemsProcessed());
            vapiSummary.put("lastSyncMessage", latestVapiSync.getMessage());

            // Count success/failures
            long successCount = syncStatusRepository.countBySyncTypeAndSuccess("VAPI_ASSISTANTS", true);
            long failureCount = syncStatusRepository.countBySyncTypeAndSuccess("VAPI_ASSISTANTS", false);

            vapiSummary.put("totalSuccessfulSyncs", successCount);
            vapiSummary.put("totalFailedSyncs", failureCount);

            summary.put("vapiAssistants", vapiSummary);
        }

        return ResponseEntity.ok(summary);
    }
}
