package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiCreateAssistantRequest;
import com.sfaai.sfaai.dto.VapiCreateAssistantResponse;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.service.VapiAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for Vapi assistant operations
 */
@RestController
@RequestMapping("/api/vapi/assistant")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vapi Assistants", description = "Endpoints for Vapi assistant management")
public class AdminVapiAgentController {

    private final VapiAgentService vapiAgentService;

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
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid API key"
        ),
        @ApiResponse(
            responseCode = "502", 
            description = "Bad Gateway - error communicating with Vapi API"
        )
    })
    @PostMapping
    public ResponseEntity<VapiCreateAssistantResponse> createAssistant(@Valid @RequestBody VapiCreateAssistantRequest request) {
        log.info("Creating new Vapi assistant with first message: {}", request.getFirstMessage());

        VapiCreateAssistantResponse response = vapiAgentService.createAssistant(request);

        log.info("Successfully created Vapi assistant with ID: {}", response.getAssistantId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all Vapi assistants
     * @return List of all assistants
     */
    @Operation(
        summary = "Get all Vapi assistants", 
        description = "Retrieves all assistants from the Vapi API"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Assistants retrieved successfully",
            content = @Content(schema = @Schema(implementation = VapiListAssistantsResponse.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - invalid API key"
        ),
        @ApiResponse(
            responseCode = "502", 
            description = "Bad Gateway - error communicating with Vapi API"
        )
    })
    @GetMapping
    public ResponseEntity<VapiListAssistantsResponse> getAllAssistants() {
        log.info("Retrieving all Vapi assistants");

        VapiListAssistantsResponse response = vapiAgentService.getAllAssistants();

        log.info("Successfully retrieved {} Vapi assistants", 
                response.getAssistants() != null ? response.getAssistants().size() : 0);

        // Debug DTO values before returning from controller
        if (response.getAssistants() != null && !response.getAssistants().isEmpty()) {
            log.debug("CONTROLLER DEBUG: Inspecting assistants before returning response");
            for (int i = 0; i < Math.min(3, response.getAssistants().size()); i++) {
                VapiAssistantDTO assistant = response.getAssistants().get(i);
                log.debug("Assistant #{}: ID={}, Name={}", i+1, assistant.getAssistantId(), assistant.getName());
                log.debug("  - firstMessage: '{}'", assistant.getFirstMessage());

                if (assistant.getFirstMessage() == null) {
                    log.debug("  - WARNING: firstMessage is null for this assistant");
                }
            }
        }

        return ResponseEntity.ok(response);
    }
}
