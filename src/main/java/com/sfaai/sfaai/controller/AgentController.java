package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "Agent Management", description = "Endpoints for managing AI agents")
public class AgentController {
    private final AgentService agentService;

    /**
     * Creates a new agent
     * @param dto The agent details
     * @return The created agent
     */
    @Operation(summary = "Create a new agent", description = "Creates a new agent in the system",
            responses = {
                @ApiResponse(responseCode = "201", description = "Agent created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "403", description = "Not authorized to create agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AgentDTO> create(@Valid @RequestBody AgentCreateDTO dto) {
        AgentDTO created = agentService.createAgent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get agent by ID
     * @param id Agent ID
     * @return The agent details
     */
    @Operation(summary = "Get agent by ID", description = "Retrieves an agent by its ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agent found",
                        content = @Content(schema = @Schema(implementation = AgentDTO.class))),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view this agent")
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getById(@PathVariable Long id) {
        try {
            AgentDTO agent = agentService.getAgent(id);
            return ResponseEntity.ok(agent);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all agents with optional pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return List of agents
     */
    @Operation(summary = "Get all agents", description = "Retrieves all agents with pagination support",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view agents")
            })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<AgentDTO>> getAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        List<AgentDTO> agents = agentService.getAllAgents(page, size);
        return ResponseEntity.ok(agents);
    }

    /**
     * Update an existing agent
     * @param id Agent ID to update
     * @param dto Updated agent details
     * @return The updated agent
     */
    @Operation(summary = "Update agent", description = "Updates an existing agent's details",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agent updated successfully"),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "403", description = "Not authorized to update agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> update(@PathVariable Long id, @Valid @RequestBody AgentDTO dto) {
        AgentDTO updated = agentService.updateAgent(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an agent
     * @param id Agent ID to delete
     * @return No content response
     */
    @Operation(summary = "Delete agent", description = "Deletes an agent from the system",
            responses = {
                @ApiResponse(responseCode = "204", description = "Agent deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Agent not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to delete agents")
            })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get agents by client ID
     * @param clientId Client ID
     * @return List of agents for the specified client
     */
    @Operation(summary = "Get agents by client", description = "Retrieves all agents belonging to a specific client",
            responses = {
                @ApiResponse(responseCode = "200", description = "Agents retrieved successfully"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view these agents")
            })
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<AgentDTO>> getAgentsByClient(@PathVariable Long clientId) {
        List<AgentDTO> agents = agentService.getAgentsByClientId(clientId);
        return ResponseEntity.ok(agents);
    }
}