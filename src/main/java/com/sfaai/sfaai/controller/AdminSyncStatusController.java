package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.entity.SyncStatus;
import com.sfaai.sfaai.repository.SyncStatusRepository;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for viewing synchronization status
 */
@RestController
@RequestMapping("/api/admin/sync-status")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sync Status", description = "Endpoints for viewing synchronization status")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSyncStatusController {

    private final SyncStatusRepository syncStatusRepository;
    private final VapiAssistantSyncService vapiAssistantSyncService;

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
    @GetMapping
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
    @GetMapping("/summary")
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

    /**
     * Manually trigger a Vapi assistant sync
     * @return Result of sync operation
     */
    @Operation(
        summary = "Trigger Vapi assistant sync", 
        description = "Manually triggers synchronization of Vapi assistants"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sync triggered successfully"),
        @ApiResponse(responseCode = "500", description = "Error during synchronization")
    })
    @PostMapping("/vapi-assistants/trigger")
    public ResponseEntity<Map<String, Object>> triggerVapiAssistantSync() {
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
}
