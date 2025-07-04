package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.SimpleUserRequest;
import com.sfaai.sfaai.service.ClientService;
import com.sfaai.sfaai.service.SecurityService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
public class ClientController {

    private final ClientService clientService;

    /**
     * Create a new client/user
     * @param dto Client creation data
     * @return Created client DTO
     */
    @Operation(summary = "Create a new user", description = "Creates a new user in the system",
            responses = {
                @ApiResponse(responseCode = "201", description = "Client created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClientDTO> create(@Valid @RequestBody ClientCreateDTO dto) {
        ClientDTO created = clientService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Create a new client/user (simple version for testing)
     * @param request Simple user creation request
     * @return Created client DTO
     */
    @Operation(summary = "Create a new user (simple)", description = "Creates a new user in the system with minimal validation",
            responses = {
                @ApiResponse(responseCode = "201", description = "Client created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    @PostMapping("/simple")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> createSimple(@RequestBody SimpleUserRequest request) {
        System.out.println("DEBUG: Received request - fullName: " + request.getFullName() + ", email: " + request.getEmail());
        
        ClientCreateDTO dto = ClientCreateDTO.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .confirmPassword(request.getPassword())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .agree(true)
                .passwordMatching(true)
                .build();
        
        System.out.println("DEBUG: Created DTO - fullName: " + dto.getFullName() + ", email: " + dto.getEmail());
        
        ClientDTO created = clientService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get all clients with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @return List of clients
     */
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination support",
            responses = {
                @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view clients")
            })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientDTO>> all(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(clientService.findAll(page, size));
    }

    /**
     * Get client by ID
     * @param id Client ID
     * @return The client details
     */
    @Operation(summary = "Get user by ID", description = "Retrieves a user by its ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Client found",
                        content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                @ApiResponse(responseCode = "404", description = "Client not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to view this client")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
        ClientDTO client = clientService.findById(id);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(client);
    }

    /**
     * Delete a client
     * @param id Client ID to delete
     * @return No content response
     */
    @Operation(summary = "Delete user", description = "Deletes a user from the system",
            responses = {
                @ApiResponse(responseCode = "204", description = "Client deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Client not found"),
                @ApiResponse(responseCode = "403", description = "Not authorized to delete this client")
            })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
