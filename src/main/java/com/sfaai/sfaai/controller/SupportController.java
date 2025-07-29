package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.SupportRequestCreateDTO;
import com.sfaai.sfaai.dto.SupportRequestDTO;
import com.sfaai.sfaai.service.SupportRequestService;
import com.sfaai.sfaai.service.impl.CustomUserDetailsService;
import com.sfaai.sfaai.repository.ClientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Support", description = "Endpoints for managing support requests")
public class SupportController {

    private final SupportRequestService supportRequestService;
    private final ClientRepository clientRepository;

    /**
     * Create a new support request
     * @param createDTO Support request creation data
     * @param authentication Current user authentication
     * @return Success message
     */
    @Operation(
        summary = "Create support request",
        description = "Creates a new support request for the authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support request submitted successfully",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(responseCode = "400", description = "Missing required fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> createSupportRequest(
            @Valid @RequestBody SupportRequestCreateDTO createDTO,
            Authentication authentication) {
        
        log.info("Support request received from user: {}", authentication.getName());
        
        try {
            // Extract user information from authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Override user information from authentication if not provided
            if (createDTO.getUserId() == null || createDTO.getUserId().trim().isEmpty()) {
                createDTO.setUserId(userDetails.getUsername());
            }
            
            if (createDTO.getUserEmail() == null || createDTO.getUserEmail().trim().isEmpty()) {
                createDTO.setUserEmail(userDetails.getUsername());
            }
            
            // Create the support request
            SupportRequestDTO createdRequest = supportRequestService.createSupportRequest(createDTO);
            
            log.info("Support request created successfully with ID: {}", createdRequest.getId());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Support request submitted successfully");
            response.put("requestId", createdRequest.getId().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid support request data: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error creating support request", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get support request by ID (Admin only)
     * @param id Support request ID
     * @return Support request details
     */
    @Operation(
        summary = "Get support request by ID",
        description = "Retrieves a specific support request by its ID (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support request retrieved successfully",
            content = @Content(schema = @Schema(implementation = SupportRequestDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Support request not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportRequestDTO> getSupportRequestById(@PathVariable Long id) {
        log.debug("Fetching support request by ID: {}", id);
        
        SupportRequestDTO supportRequest = supportRequestService.getSupportRequestById(id);
        return ResponseEntity.ok(supportRequest);
    }

    /**
     * Get all support requests with pagination (Admin only)
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Page of support requests
     */
    @Operation(
        summary = "Get all support requests",
        description = "Retrieves all support requests with pagination (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SupportRequestDTO>> getAllSupportRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching all support requests - page: {}, size: {}", page, size);
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<SupportRequestDTO> supportRequests = supportRequestService.getAllSupportRequests(pageRequest);
        
        return ResponseEntity.ok(supportRequests);
    }

    /**
     * Get support requests by user ID (Admin only)
     * @param userId User ID
     * @return List of support requests for the user
     */
    @Operation(
        summary = "Get support requests by user ID",
        description = "Retrieves all support requests for a specific user (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportRequestDTO>> getSupportRequestsByUserId(@PathVariable String userId) {
        log.debug("Fetching support requests for user ID: {}", userId);
        
        List<SupportRequestDTO> supportRequests = supportRequestService.getSupportRequestsByUserId(userId);
        return ResponseEntity.ok(supportRequests);
    }

    /**
     * Get support requests by status (Admin only)
     * @param status Support request status
     * @return List of support requests with the specified status
     */
    @Operation(
        summary = "Get support requests by status",
        description = "Retrieves all support requests with a specific status (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportRequestDTO>> getSupportRequestsByStatus(@PathVariable String status) {
        log.debug("Fetching support requests by status: {}", status);
        
        try {
            List<SupportRequestDTO> supportRequests = supportRequestService.getSupportRequestsByStatus(status);
            return ResponseEntity.ok(supportRequests);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status provided: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update support request status (Admin only)
     * @param id Support request ID
     * @param status New status
     * @return Updated support request
     */
    @Operation(
        summary = "Update support request status",
        description = "Updates the status of a support request (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support request status updated successfully",
            content = @Content(schema = @Schema(implementation = SupportRequestDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Support request not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportRequestDTO> updateSupportRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("Updating support request status - ID: {}, Status: {}", id, status);
        
        try {
            SupportRequestDTO updatedRequest = supportRequestService.updateSupportRequestStatus(id, status);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status provided: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete support request (Admin only)
     * @param id Support request ID
     * @return No content response
     */
    @Operation(
        summary = "Delete support request",
        description = "Deletes a support request (Admin only)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Support request deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Support request not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSupportRequest(@PathVariable Long id) {
        log.info("Deleting support request with ID: {}", id);
        
        supportRequestService.deleteSupportRequest(id);
        return ResponseEntity.noContent().build();
    }

    // User endpoints for viewing their own support requests
    /**
     * Get current user's support requests
     * @param authentication Current user authentication
     * @return List of user's support requests
     */
    @Operation(
        summary = "Get current user's support requests",
        description = "Retrieves all support requests for the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SupportRequestDTO>> getMySupportRequests(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("User {} requesting their support requests", userEmail);
        
        // Get the actual user ID from the database
        String userId = clientRepository.findByEmail(userEmail)
                .map(client -> client.getId().toString())
                .orElse(userEmail); // Fallback to email if user not found
        
        log.info("User email: {}, User ID: {}", userEmail, userId);
        
        List<SupportRequestDTO> supportRequests = supportRequestService.getSupportRequestsByUserId(userId);
        return ResponseEntity.ok(supportRequests);
    }

    /**
     * Get current user's support requests with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @param authentication Current user authentication
     * @return Page of user's support requests
     */
    @Operation(
        summary = "Get current user's support requests (paginated)",
        description = "Retrieves paginated support requests for the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SupportRequestDTO>> getMySupportRequestsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} requesting their paginated support requests - page: {}, size: {}", userEmail, page, size);
        
        // Get the actual user ID from the database
        String userId = clientRepository.findByEmail(userEmail)
                .map(client -> client.getId().toString())
                .orElse(userEmail); // Fallback to email if user not found
        
        log.info("User email: {}, User ID: {}", userEmail, userId);
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<SupportRequestDTO> supportRequests = supportRequestService.getSupportRequestsByUserIdPaginated(userId, pageRequest);
        return ResponseEntity.ok(supportRequests);
    }

    /**
     * Get specific support request by ID (user can only see their own)
     * @param id Support request ID
     * @param authentication Current user authentication
     * @return Support request details
     */
    @Operation(
        summary = "Get specific support request by ID",
        description = "Retrieves a specific support request by ID (user can only see their own requests)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support request retrieved successfully",
            content = @Content(schema = @Schema(implementation = SupportRequestDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Support request not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - not your request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportRequestDTO> getMySupportRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} requesting support request with ID: {}", userEmail, id);
        
        // Get the actual user ID from the database
        String userId = clientRepository.findByEmail(userEmail)
                .map(client -> client.getId().toString())
                .orElse(userEmail); // Fallback to email if user not found
        
        log.info("User email: {}, User ID: {}", userEmail, userId);
        
        try {
            SupportRequestDTO supportRequest = supportRequestService.getSupportRequestByIdAndUserId(id, userId);
            return ResponseEntity.ok(supportRequest);
        } catch (IllegalArgumentException e) {
            log.warn("Support request not found or access denied - ID: {}, User: {}", id, userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get current user's support requests by status
     * @param status Support request status
     * @param authentication Current user authentication
     * @return List of user's support requests with specified status
     */
    @Operation(
        summary = "Get current user's support requests by status",
        description = "Retrieves support requests with specific status for the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SupportRequestDTO>> getMySupportRequestsByStatus(
            @PathVariable String status,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        log.info("User {} requesting their support requests with status: {}", userEmail, status);
        
        // Get the actual user ID from the database
        String userId = clientRepository.findByEmail(userEmail)
                .map(client -> client.getId().toString())
                .orElse(userEmail); // Fallback to email if user not found
        
        log.info("User email: {}, User ID: {}", userEmail, userId);
        
        try {
            List<SupportRequestDTO> supportRequests = supportRequestService.getSupportRequestsByUserIdAndStatus(userId, status);
            return ResponseEntity.ok(supportRequests);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status provided: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user's support request count
     * @param authentication Current user authentication
     * @return Count of user's support requests
     */
    @Operation(
        summary = "Get current user's support request count",
        description = "Retrieves the count of support requests for the currently authenticated user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Support request count retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-requests/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMySupportRequestCount(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("User {} requesting their support request count", userEmail);
        
        // Get the actual user ID from the database
        String userId = clientRepository.findByEmail(userEmail)
                .map(client -> client.getId().toString())
                .orElse(userEmail); // Fallback to email if user not found
        
        log.info("User email: {}, User ID: {}", userEmail, userId);
        
        long count = supportRequestService.getSupportRequestCountByUserId(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("userEmail", userEmail);
        response.put("totalRequests", count);
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
} 