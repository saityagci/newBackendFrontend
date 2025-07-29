package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.ClientUpdateDTO;
import com.sfaai.sfaai.dto.PasswordUpdateRequest;
import com.sfaai.sfaai.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile Management", description = "Endpoints for managing user profile")
public class ProfileController {

    private final ClientService clientService;

    /**
     * Get the currently authenticated user's profile
     * @param userDetails The authenticated user details
     * @return The client DTO for the authenticated user
     */
    @Operation(summary = "Get user profile", description = "Retrieves the currently authenticated user's profile information",
            responses = {
                @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                        content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> getProfile(@org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        try {
            ClientDTO user = clientService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error retrieving profile for user: {}", email, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update the currently authenticated user's profile
     * @param userDetails The authenticated user details
     * @param dto Updated profile data
     * @return Updated client DTO
     */
    @Operation(summary = "Update user profile", description = "Updates the currently authenticated user's profile information",
            responses = {
                @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                        content = @Content(schema = @Schema(implementation = ClientDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> updateProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ClientUpdateDTO dto) {
        try {
            log.info("=== PROFILE UPDATE REQUEST ===");
            log.info("User: {}", userDetails.getUsername());
            log.info("Request DTO: {}", dto);
            log.info("fullName in DTO: '{}'", dto.getFullName());
            log.info("email in DTO: '{}'", dto.getEmail());
            log.info("phone in DTO: '{}'", dto.getPhone());
            
            ClientDTO currentUser = clientService.findByEmail(userDetails.getUsername());
            log.info("Current user found: ID={}, fullName='{}'", currentUser.getId(), currentUser.getFullName());
            
            ClientDTO updated = clientService.update(currentUser.getId(), dto);
            log.info("Update completed. New fullName: '{}'", updated.getFullName());
            log.info("=== END PROFILE UPDATE ===");
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", userDetails.getUsername(), e);
            // Return 500 for unexpected errors instead of 404
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update the currently authenticated user's password
     * @param userDetails The authenticated user details
     * @param passwordUpdateRequest Password update request
     * @return Success response
     */
    @Operation(summary = "Update user password", description = "Updates the currently authenticated user's password with current password verification",
            responses = {
                @ApiResponse(responseCode = "200", description = "Password updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid password data or current password incorrect"),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> updatePassword(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        try {
            log.info("=== PASSWORD UPDATE REQUEST ===");
            log.info("User: {}", userDetails.getUsername());
            
            // Validate that new password and confirm password match
            if (!passwordUpdateRequest.getNewPassword().equals(passwordUpdateRequest.getConfirmPassword())) {
                log.warn("Password mismatch for user: {}", userDetails.getUsername());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password and confirm password do not match"));
            }
            
            ClientDTO currentUser = clientService.findByEmail(userDetails.getUsername());
            log.info("Current user found: ID={}", currentUser.getId());
            
            clientService.updatePasswordWithVerification(
                currentUser.getId(), 
                passwordUpdateRequest.getCurrentPassword(), 
                passwordUpdateRequest.getNewPassword()
            );
            
            log.info("Password updated successfully for user: {}", userDetails.getUsername());
            log.info("=== END PASSWORD UPDATE ===");
            
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Password update failed for user: {} - {}", userDetails.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating password for user: {}", userDetails.getUsername(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }
} 