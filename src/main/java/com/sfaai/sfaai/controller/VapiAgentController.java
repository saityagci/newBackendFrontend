package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VapiAgentRequestDTO;
import com.sfaai.sfaai.dto.VapiAgentResponseDTO;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.service.VapiAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Vapi voice agent operations
 */
@RestController
@RequestMapping("/api/vapi-agent")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vapi Voice Agents", description = "Endpoints for Vapi voice agent management")
public class VapiAgentController {

    private final VapiAgentService vapiAgentService;

    /**
     * Create a new Vapi voice agent
     * @param requestDTO Agent creation data
     * @return Created agent
     */
    @Operation(summary = "Create a new Vapi voice agent", description = "Creates a new agent via the Vapi API",
            responses = {
                @ApiResponse(responseCode = "201", description = "Agent created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "403", description = "Not authorized to create agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VapiAgentResponseDTO> createVapiAgent(@Valid @RequestBody VapiAgentRequestDTO requestDTO) {
        log.info("Creating Vapi agent with name: {}", requestDTO.getName());
        VapiAgentResponseDTO response = vapiAgentService.createVapiAgent(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get Vapi agent by ID
     * @param id Agent ID
     * @return The agent details
     */
    @Operation(summary = "Get Vapi agent by ID", description = "Retrieves a Vapi agent by its ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agent found"),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view this agent")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VapiAgentResponseDTO> getVapiAgentById(@PathVariable Long id) {
        try {
            VapiAgentResponseDTO agent = vapiAgentService.getVapiAgent(id);
            return ResponseEntity.ok(agent);
        } catch (ResourceNotFoundException e) {
            log.warn("Vapi agent not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get Vapi agent by external Vapi ID
     * @param vapiId External Vapi agent ID
     * @return The agent details
     */
    @Operation(summary = "Get Vapi agent by external ID", description = "Retrieves a Vapi agent by its external Vapi ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agent found"),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view this agent")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/external/{vapiId}")
    public ResponseEntity<VapiAgentResponseDTO> getVapiAgentByExternalId(@PathVariable String vapiId) {
        try {
            VapiAgentResponseDTO agent = vapiAgentService.getVapiAgentByExternalId(vapiId);
            return ResponseEntity.ok(agent);
        } catch (ResourceNotFoundException e) {
            log.warn("Vapi agent not found with external ID: {}", vapiId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all Vapi agents with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Page of agents
     */
    @Operation(summary = "Get all Vapi agents", description = "Retrieves all Vapi agents with pagination",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<VapiAgentResponseDTO>> getAllVapiAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VapiAgentResponseDTO> agents = vapiAgentService.getAllVapiAgents(PageRequest.of(page, size));
        return ResponseEntity.ok(agents);
    }

    /**
     * Get Vapi agents by client ID
     * @param clientId Client ID
     * @return List of agents for the client
     */
    @Operation(summary = "Get Vapi agents by client", description = "Retrieves all Vapi agents for a specific client",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "Client not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view these agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<VapiAgentResponseDTO>> getVapiAgentsByClient(@PathVariable Long clientId) {
        try {
            List<VapiAgentResponseDTO> agents = vapiAgentService.getVapiAgentsByClientId(clientId);
            return ResponseEntity.ok(agents);
        } catch (ResourceNotFoundException e) {
            log.warn("Client not found with ID: {}", clientId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a Vapi agent
     * @param id Agent ID to delete
     * @return No content response
     */
    @Operation(summary = "Delete Vapi agent", description = "Deletes a Vapi agent from both Vapi API and local database",
            responses = {
                @ApiResponse(responseCode = "204", description = "Agent deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to delete agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVapiAgent(@PathVariable Long id) {
        try {
            vapiAgentService.deleteVapiAgent(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Vapi agent not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
