package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.ClientUpdateDTO;
import com.sfaai.sfaai.dto.PasswordUpdateRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
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
        log.debug("Received request - fullName: {}, email: {}", request.getFullName(), request.getEmail());
        
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
        
        log.debug("Created DTO - fullName: {}, email: {}", dto.getFullName(), dto.getEmail());
        
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
    public ResponseEntity<Page<ClientDTO>> all(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ResponseEntity.ok(clientService.findAll(PageRequest.of(page, size)));
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
     * Update a client/user
     * @param id Client ID to update
     * @param dto Updated client data
     * @return Updated client DTO
     */
    @Operation(summary = "Update user", description = "Updates an existing user's information",
            responses = {
                @ApiResponse(responseCode = "200", description = "Client updated successfully",
                        content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                @ApiResponse(responseCode = "404", description = "Client not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "403", description = "Not authorized to update this client")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> update(@PathVariable Long id, @Valid @RequestBody ClientUpdateDTO dto) {
        try {
            ClientDTO updated = clientService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating client with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a client/user's password
     * @param id Client ID to update
     * @param passwordUpdateRequest Password update request
     * @return Success response
     */
    @Operation(summary = "Update user password", description = "Updates a user's password",
            responses = {
                @ApiResponse(responseCode = "200", description = "Password updated successfully"),
                @ApiResponse(responseCode = "404", description = "Client not found"),
                @ApiResponse(responseCode = "400", description = "Invalid password data"),
                @ApiResponse(responseCode = "403", description = "Not authorized to update this client")
            })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @PathVariable Long id, 
            @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        try {
            clientService.updatePassword(id, passwordUpdateRequest.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            log.error("Error updating password for client with ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
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

    /**
     * Get the currently authenticated user's information
     * @param userDetails The authenticated user details
     * @return The client DTO for the authenticated user
     */
    @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's information",
            responses = {
                @ApiResponse(responseCode = "200", description = "Current user retrieved successfully",
                        content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        try {
            ClientDTO user = clientService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
