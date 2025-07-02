package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.service.ElevenLabsVoiceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for ElevenLabs voice log operations
 * Provides endpoints for viewing and managing ElevenLabs voice logs
 */
@RestController
@RequestMapping("/api/elevenlabs/voicelogs")
@RequiredArgsConstructor
@Tag(name = "ElevenLabs Voice Logs", description = "ElevenLabs voice logs management API")
@Slf4j
public class ElevenLabsVoiceLogController {

    private final ElevenLabsVoiceLogService elevenLabsVoiceLogService;

    /**
     * Get all ElevenLabs voice logs
     * @return List of ElevenLabs voice log DTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all ElevenLabs voice logs",
        description = "Retrieves all ElevenLabs voice logs from the database"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved ElevenLabs voice logs",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VoiceLogDTO.class)
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<VoiceLogDTO>> getAllElevenLabsVoiceLogs() {
        log.info("Retrieving all ElevenLabs voice logs");
        List<VoiceLogDTO> voiceLogs = elevenLabsVoiceLogService.getAllElevenLabsVoiceLogs();
        log.info("Found {} ElevenLabs voice logs", voiceLogs.size());
        return ResponseEntity.ok(voiceLogs);
    }

    /**
     * Get ElevenLabs voice logs with pagination
     * @param pageable Pagination parameters
     * @return Page of ElevenLabs voice log DTOs
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get ElevenLabs voice logs with pagination",
        description = "Retrieves ElevenLabs voice logs with pagination support"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved ElevenLabs voice logs",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<VoiceLogDTO>> getElevenLabsVoiceLogs(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Retrieving ElevenLabs voice logs with pagination: {}", pageable);
        Page<VoiceLogDTO> voiceLogs = elevenLabsVoiceLogService.getElevenLabsVoiceLogs(pageable);
        log.info("Found {} ElevenLabs voice logs (page {} of {})", 
                voiceLogs.getContent().size(), voiceLogs.getNumber() + 1, voiceLogs.getTotalPages());
        return ResponseEntity.ok(voiceLogs);
    }

    /**
     * Get a specific ElevenLabs voice log by external call ID
     * @param externalCallId The ElevenLabs conversation ID
     * @return Voice log DTO if found
     */
    @GetMapping("/{externalCallId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get ElevenLabs voice log by external call ID",
        description = "Retrieves a specific ElevenLabs voice log by its external call ID (conversation ID)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved ElevenLabs voice log",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VoiceLogDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Voice log not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VoiceLogDTO> getElevenLabsVoiceLogByExternalCallId(
            @Parameter(description = "ElevenLabs conversation ID") 
            @PathVariable String externalCallId) {
        log.info("Retrieving ElevenLabs voice log with external call ID: {}", externalCallId);
        
        VoiceLogDTO voiceLog = elevenLabsVoiceLogService.getElevenLabsVoiceLogByExternalCallId(externalCallId);
        
        if (voiceLog == null) {
            log.warn("ElevenLabs voice log not found with external call ID: {}", externalCallId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Successfully retrieved ElevenLabs voice log with external call ID: {}", externalCallId);
        return ResponseEntity.ok(voiceLog);
    }

    /**
     * Stream/download audio for a specific ElevenLabs voice log
     * @param externalCallId The ElevenLabs conversation ID
     * @return Audio resource
     */
    @GetMapping("/{externalCallId}/audio")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get ElevenLabs voice log audio",
        description = "Streams/downloads the audio recording for a specific ElevenLabs voice log"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved audio",
            content = @Content(mediaType = "audio/*")
        ),
        @ApiResponse(responseCode = "404", description = "Voice log or audio not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Resource> getElevenLabsVoiceLogAudio(
            @Parameter(description = "ElevenLabs conversation ID") 
            @PathVariable String externalCallId) {
        log.info("Retrieving audio for ElevenLabs voice log with external call ID: {}", externalCallId);
        
        Resource audioResource = elevenLabsVoiceLogService.getElevenLabsVoiceLogAudio(externalCallId);
        
        if (audioResource == null) {
            log.warn("Audio not found for ElevenLabs voice log with external call ID: {}", externalCallId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Successfully retrieved audio for ElevenLabs voice log with external call ID: {}", externalCallId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"elevenlabs-audio-" + externalCallId + ".mp3\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(audioResource);
    }

    /**
     * Manual sync trigger for ElevenLabs voice logs
     * @return Sync summary
     */
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Sync ElevenLabs voice logs",
        description = "Manually triggers synchronization of voice logs from ElevenLabs API"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Sync completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Map<String, Object>> syncElevenLabsVoiceLogs() {
        log.info("Manual ElevenLabs voice logs sync triggered via API");
        
        ElevenLabsVoiceLogService.SyncSummary summary = elevenLabsVoiceLogService.manualSync();
        
        Map<String, Object> result = Map.of(
            "success", summary.errors == 0,
            "message", summary.errors == 0 ? "Successfully synchronized ElevenLabs voice logs" : "Errors occurred during sync",
            "fetched", summary.fetched,
            "updated", summary.updated,
            "skipped", summary.skipped,
            "errors", summary.errors,
            "durationMs", summary.durationMs
        );
        
        log.info("Manual sync result: {} fetched, {} updated, {} skipped, {} errors, {} ms", 
                summary.fetched, summary.updated, summary.skipped, summary.errors, summary.durationMs);
        
        return ResponseEntity.ok(result);
    }
} 