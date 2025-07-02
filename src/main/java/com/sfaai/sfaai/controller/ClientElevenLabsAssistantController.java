package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.AssignAssistantRequest;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.service.ClientService;
import com.sfaai.sfaai.service.SecurityService;
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

import java.util.List;

/**
 * Controller for client-side ElevenLabs assistant operations
 * Allows clients to view and interact with their assigned ElevenLabs assistants
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client ElevenLabs Assistants", description = "Endpoints for client-side ElevenLabs assistant management")
public class ClientElevenLabsAssistantController {

    private final ClientService clientService;
    private final SecurityService securityService;

    /**
     * Get all ElevenLabs assistants assigned to a client
     * @param clientId Client ID
     * @return List of ElevenLabs assistants assigned to the client
     */
    @Operation(
            summary = "Get ElevenLabs assistants assigned to client", 
            description = "Retrieves all ElevenLabs assistants assigned to a specific client from the database. A client can have multiple assistants assigned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistants retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ElevenLabsAssistantDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view assistants")
    })
    @GetMapping("/{clientId}/elevenlabs-assistants")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ElevenLabsAssistantDTO>> getClientElevenLabsAssistants(@PathVariable Long clientId) {
        log.info("Retrieving all ElevenLabs assistants assigned to client ID: {}", clientId);

        // Check if user has access to this client
        if (!securityService.hasClientAccess(clientId)) {
            log.warn("Access denied: User does not have permission to view client {}", clientId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<ElevenLabsAssistantDTO> assistants = clientService.getAssignedElevenLabsAssistants(clientId);

            log.info("Found {} ElevenLabs assistants assigned to client ID: {}", assistants.size(), clientId);

            return ResponseEntity.ok(assistants);
        } catch (ResourceNotFoundException e) {
            log.error("Client not found with ID: {}", clientId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving ElevenLabs assistants for client ID: {}", clientId, e);
            throw e;
        }
    }

    /**
     * Get a specific ElevenLabs assistant assigned to a client
     * @param clientId Client ID
     * @param assistantId Assistant ID
     * @return Assistant DTO if found and assigned to client
     */
    @Operation(
            summary = "Get specific ElevenLabs assistant assigned to client",
            description = "Retrieves a specific ElevenLabs assistant assigned to a client"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assistant retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ElevenLabsAssistantDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Assistant not found or not assigned to client"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this assistant")
    })
    @GetMapping("/{clientId}/elevenlabs-assistants/{assistantId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ElevenLabsAssistantDTO> getClientElevenLabsAssistant(
            @PathVariable Long clientId,
            @PathVariable String assistantId) {

        log.info("Retrieving ElevenLabs assistant ID: {} for client ID: {}", assistantId, clientId);

        // Check if user has access to this client
        if (!securityService.hasClientAccess(clientId)) {
            log.warn("Access denied: User does not have permission to view client {}", clientId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            ElevenLabsAssistantDTO assistant = clientService.getAssignedElevenLabsAssistants(clientId).stream()
                    .filter(a -> a.getAssistantId().equals(assistantId))
                    .findFirst()
                    .orElse(null);

            if (assistant == null) {
                log.warn("ElevenLabs assistant ID: {} not found or not assigned to client ID: {}", assistantId, clientId);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(assistant);
        } catch (ResourceNotFoundException e) {
            log.error("Client not found with ID: {}", clientId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving ElevenLabs assistant ID: {} for client ID: {}", assistantId, clientId, e);
            throw e;
        }
    }

    //--------------------------------------------------------------------------
    // Admin-only endpoints for managing client-assistant relationships
    //--------------------------------------------------------------------------

    /**
     * Assign an ElevenLabs assistant to a client
     * @param clientId Client ID
     * @param request Request containing ElevenLabs assistant ID
     * @return Updated client DTO
     */
    @Operation(
            summary = "Assign ElevenLabs assistant to client", 
            description = "Assigns a specific ElevenLabs assistant to a client"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistant successfully assigned to client",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to assign assistants")
    })
    @PostMapping("/{clientId}/assign-elevenlabs-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> assignElevenLabsAssistantToClient(
            @PathVariable Long clientId, 
            @Valid @RequestBody AssignAssistantRequest request) {

        log.info("Assigning ElevenLabs assistant ID {} to client ID {}", request.getElevenLabsAssistantId(), clientId);

        ClientDTO updatedClient = clientService.assignElevenLabsAssistant(clientId, request.getElevenLabsAssistantId());

        log.info("Successfully assigned ElevenLabs assistant to client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Unassign (remove) the ElevenLabs assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO with elevenLabsAssistantId set to null
     */
    @Operation(
            summary = "Unassign ElevenLabs assistant from client", 
            description = "Removes the ElevenLabs assistant assignment from a client"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistant successfully unassigned from client",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to unassign assistants")
    })
    @PostMapping("/{clientId}/unassign-elevenlabs-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> unassignElevenLabsAssistantFromClient(@PathVariable Long clientId) {

        log.info("Unassigning ElevenLabs assistant from client ID {}", clientId);

        ClientDTO updatedClient = clientService.unassignElevenLabsAssistant(clientId);

        log.info("Successfully unassigned ElevenLabs assistant from client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Add an ElevenLabs assistant to a client's list of assistants
     * @param clientId Client ID
     * @param request Request containing ElevenLabs assistant ID
     * @return Updated client DTO
     */
    @Operation(
            summary = "Add ElevenLabs assistant to client", 
            description = "Adds an ElevenLabs assistant to a client's list of assistants"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistant successfully added to client",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add assistants")
    })
    @PostMapping("/{clientId}/add-elevenlabs-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> addElevenLabsAssistantToClient(
            @PathVariable Long clientId, 
            @Valid @RequestBody AssignAssistantRequest request) {

        log.info("Adding ElevenLabs assistant ID {} to client ID {}", request.getElevenLabsAssistantId(), clientId);

        ClientDTO updatedClient = clientService.addElevenLabsAssistant(clientId, request.getElevenLabsAssistantId());

        log.info("Successfully added ElevenLabs assistant to client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Remove an ElevenLabs assistant from a client's list of assistants
     * @param clientId Client ID
     * @param assistantId Assistant ID to remove
     * @return Updated client DTO
     */
    @Operation(
            summary = "Remove ElevenLabs assistant from client", 
            description = "Removes an ElevenLabs assistant from a client's list of assistants"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Assistant successfully removed from client",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to remove assistants")
    })
    @PostMapping("/{clientId}/remove-elevenlabs-assistant/{assistantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> removeElevenLabsAssistantFromClient(
            @PathVariable Long clientId,
            @PathVariable String assistantId) {

        log.info("Removing ElevenLabs assistant ID {} from client ID {}", assistantId, clientId);

        ClientDTO updatedClient = clientService.removeElevenLabsAssistant(clientId, assistantId);

        log.info("Successfully removed ElevenLabs assistant from client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Get all clients assigned to a specific ElevenLabs assistant
     * Admin-only endpoint to see which clients are using a particular assistant
     * @param assistantId The ElevenLabs assistant ID
     * @return List of clients assigned to the assistant
     */
    @Operation(
            summary = "Get clients by ElevenLabs assistant ID", 
            description = "Retrieves all clients assigned to a specific ElevenLabs assistant"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Clients retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ClientDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Not authorized to view clients")
    })
    @GetMapping("/elevenlabs-assistants/{assistantId}/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getClientsByElevenLabsAssistantId(@PathVariable String assistantId) {
        log.info("Retrieving all clients assigned to ElevenLabs assistant ID: {}", assistantId);

        List<ClientDTO> clients = clientService.findClientsByElevenLabsAssistantId(assistantId);

        log.info("Found {} clients assigned to ElevenLabs assistant ID: {}", clients.size(), assistantId);

        return ResponseEntity.ok(clients);
    }
} 