package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.AssignAssistantRequest;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.VapiAssistantDTO;
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
 * Controller for client-Vapi integration operations
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Client Vapi Integration", description = "Endpoints for managing client and assistant integrations")
public class ClientVapiController {

    private final ClientService clientService;
    private final SecurityService securityService;

    /**
     * Assign a Vapi assistant to a client
     * @param clientId Client ID
     * @param request Request containing Vapi assistant ID
     * @return Updated client DTO
     */
    @Operation(
        summary = "Assign Vapi assistant to client", 
        description = "Assigns a specific Vapi assistant to a client"
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
    @PostMapping("/api/clients/{clientId}/assign-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> assignAssistantToClient(
            @PathVariable Long clientId, 
            @Valid @RequestBody AssignAssistantRequest request) {

        log.info("Assigning Vapi assistant ID {} to client ID {}", request.getVapiAssistantId(), clientId);

        ClientDTO updatedClient = clientService.assignVapiAssistant(clientId, request.getVapiAssistantId());

        log.info("Successfully assigned Vapi assistant to client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Unassign (remove) the Vapi assistant from a client
     * @param clientId Client ID
     * @return Updated client DTO with vapiAssistantId set to null
     */
    @Operation(
        summary = "Unassign Vapi assistant from client", 
        description = "Removes the Vapi assistant assignment from a client"
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
    @PostMapping("/api/clients/{clientId}/unassign-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> unassignAssistantFromClient(@PathVariable Long clientId) {

        log.info("Unassigning Vapi assistant from client ID {}", clientId);

        ClientDTO updatedClient = clientService.unassignVapiAssistant(clientId);

        log.info("Successfully unassigned Vapi assistant from client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Get all clients assigned to a specific Vapi assistant
     * @param assistantId The Vapi assistant ID
     * @return List of clients assigned to the assistant
     */
    @Operation(
        summary = "Get clients by assistant ID", 
        description = "Retrieves all clients assigned to a specific Vapi assistant"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Clients retrieved successfully",
            content = @Content(schema = @Schema(implementation = ClientDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Not authorized to view clients")
    })
    @GetMapping("/api/assistants/{assistantId}/assigned-clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getClientsByAssistantId(@PathVariable String assistantId) {
        log.info("Retrieving all clients assigned to Vapi assistant ID: {}", assistantId);

        List<ClientDTO> clients = clientService.findClientsByAssistantId(assistantId);

        log.info("Found {} clients assigned to assistant ID: {}", clients.size(), assistantId);

        return ResponseEntity.ok(clients);
    }

    /**
     * Get all Vapi assistants assigned to a client
     * @param clientId Client ID
     * @return List of Vapi assistants assigned to the client
     */
    @Operation(
        summary = "Get assistants assigned to client", 
        description = "Retrieves all Vapi assistants assigned to a specific client from the database"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Assistants retrieved successfully",
            content = @Content(schema = @Schema(implementation = VapiAssistantDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Client not found"),
        @ApiResponse(responseCode = "403", description = "Not authorized to view assistants")
    })
    @GetMapping("/api/clients/{clientId}/assistants")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<VapiAssistantDTO>> getClientAssistants(@PathVariable Long clientId) {
        log.info("Retrieving all Vapi assistants assigned to client ID: {}", clientId);

        // Check if user has access to this client
        if (!securityService.hasClientAccess(clientId)) {
            log.warn("Access denied: User does not have permission to view client {}", clientId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<VapiAssistantDTO> assistants = clientService.getAssignedVapiAssistants(clientId);

            log.info("Found {} assistants assigned to client ID: {}", assistants.size(), clientId);

            return ResponseEntity.ok(assistants);
        } catch (ResourceNotFoundException e) {
            log.error("Client not found with ID: {}", clientId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving assistants for client ID: {}", clientId, e);
            throw e;
        }
    }

    /**
     * Add a Vapi assistant to a client
     * @param clientId Client ID
     * @param request Request containing Vapi assistant ID
     * @return Updated client DTO
     */
    @Operation(
        summary = "Add Vapi assistant to client", 
        description = "Adds a Vapi assistant to a client's list of assistants"
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
    @PostMapping("/api/clients/{clientId}/add-assistant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> addAssistantToClient(
            @PathVariable Long clientId, 
            @Valid @RequestBody AssignAssistantRequest request) {

        log.info("Adding Vapi assistant ID {} to client ID {}", request.getVapiAssistantId(), clientId);

        ClientDTO updatedClient = clientService.addVapiAssistant(clientId, request.getVapiAssistantId());

        log.info("Successfully added Vapi assistant to client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * Remove a Vapi assistant from a client
     * @param clientId Client ID
     * @param assistantId Assistant ID to remove
     * @return Updated client DTO
     */
    @Operation(
        summary = "Remove Vapi assistant from client", 
        description = "Removes a Vapi assistant from a client's list of assistants"
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
    @PostMapping("/api/clients/{clientId}/remove-assistant/{assistantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> removeAssistantFromClient(
            @PathVariable Long clientId,
            @PathVariable String assistantId) {

        log.info("Removing Vapi assistant ID {} from client ID {}", assistantId, clientId);

        ClientDTO updatedClient = clientService.removeVapiAssistant(clientId, assistantId);

        log.info("Successfully removed Vapi assistant from client {}", clientId);

        return ResponseEntity.ok(updatedClient);
    }
}
