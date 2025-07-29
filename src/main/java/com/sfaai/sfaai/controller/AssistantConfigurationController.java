package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.AssistantConfigurationDTO;
import com.sfaai.sfaai.dto.AssistantDocumentDTO;
import com.sfaai.sfaai.entity.AssistantConfiguration;
import com.sfaai.sfaai.service.AssistantConfigurationService;
import com.sfaai.sfaai.service.AssistantDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistants")
@RequiredArgsConstructor
@Slf4j
public class AssistantConfigurationController {

    private final AssistantConfigurationService assistantConfigurationService;
    private final AssistantDocumentService assistantDocumentService;

    @PostMapping("/{assistantId}/configure")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> configureAssistant(
            @PathVariable String assistantId,
            @RequestParam("subject") String subject,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) {

        try {
            log.info("Configuring assistant: {} with subject: {}", assistantId, subject);

            // Extract user information from JWT
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String clientId = authentication.getName();
            String clientEmail = authentication.getName(); // Assuming email is the principal
            String clientName = authentication.getName(); // You might want to extract this from JWT claims

            // Create configuration
            AssistantConfigurationDTO configuration = assistantConfigurationService.createConfiguration(
                    assistantId, subject, description, documents, clientId, clientEmail, clientName);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Assistant configuration updated successfully");
            response.put("assistantId", configuration.getAssistantId());
            response.put("subject", configuration.getSubject());
            response.put("description", configuration.getDescription());
            response.put("documentCount", configuration.getDocuments() != null ? configuration.getDocuments().size() : 0);
            response.put("clientInfo", Map.of(
                    "userId", configuration.getClientId(),
                    "userEmail", configuration.getClientEmail(),
                    "userName", configuration.getClientName()
            ));
            response.put("updatedAt", configuration.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request for assistant configuration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error configuring assistant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{assistantId}/configuration")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getConfiguration(@PathVariable String assistantId) {
        try {
            log.info("Getting configuration for assistant: {}", assistantId);

            // Check permissions
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String clientId = authentication.getName();

            if (!assistantConfigurationService.hasPermission(assistantId, clientId) && 
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            AssistantConfigurationDTO configuration = assistantConfigurationService.getConfigurationByAssistantId(assistantId);
            return ResponseEntity.ok(configuration);

        } catch (IllegalArgumentException e) {
            log.error("Configuration not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{assistantId}/documents")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getDocuments(@PathVariable String assistantId) {
        try {
            log.info("Getting documents for assistant: {}", assistantId);

            // Check permissions
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String clientId = authentication.getName();

            if (!assistantConfigurationService.hasPermission(assistantId, clientId) && 
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<AssistantDocumentDTO> documents = assistantDocumentService.getDocumentsByAssistantId(assistantId);
            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("Error getting documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{assistantId}/documents/{documentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable String assistantId, @PathVariable Long documentId) {
        try {
            log.info("Deleting document: {} for assistant: {}", documentId, assistantId);

            // Check permissions
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String clientId = authentication.getName();

            if (!assistantConfigurationService.hasPermission(assistantId, clientId) && 
                !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            assistantDocumentService.deleteDocument(documentId);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));

        } catch (IllegalArgumentException e) {
            log.error("Document not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // Admin-only endpoints
    @GetMapping("/configurations")
    public ResponseEntity<Page<Assist    @PreAuthorize("hasRole('ADMIN')")
    antConequestParam(defaultValue = "true") boolean inclufigurationDTO>> getAllConfigurations(
            Pageable pageable) {            @RdeDocuments,

        log.info("Getting all configurations (admin) with documents: {}", includeDocuments);
        Page<AssistantConfigurationDTO> configurations = assistantConfigurationService.getAllConfigurations(pageable, includeDocuments);
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/configurations/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssistantConfigurationDTO>> getAllConfigurationsList() {
        log.info("Getting all configurations without pagination (admin)");
        List<AssistantConfigurationDTO> configurations = assistantConfigurationService.getAllConfigurations();
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/configurations/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AssistantConfigurationDTO>> searchConfigurations(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) AssistantConfiguration.Status status,
            @RequestParam(required = false) String assistantId,
            Pageable pageable) {
        log.info("Searching configurations with filters: subject={}, clientId={}, status={}, assistantId={}", 
                subject, clientId, status, assistantId);
        Page<AssistantConfigurationDTO> configurations = assistantConfigurationService.searchConfigurations(
                subject, clientId, status, assistantId, pageable);
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/configurations/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfigurationCounts() {
        log.info("Getting configuration counts by status");
        Map<String, Object> counts = assistantConfigurationService.getConfigurationCounts();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/configurations/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AssistantConfigurationDTO>> getConfigurationsByStatus(
            @PathVariable AssistantConfiguration.Status status, Pageable pageable) {
        log.info("Getting configurations with status: {} (admin)", status);
        Page<AssistantConfigurationDTO> configurations = assistantConfigurationService.getConfigurationsByStatus(status, pageable);
        return ResponseEntity.ok(configurations);
    }

    @PutMapping("/{assistantId}/configuration/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateConfigurationStatus(
            @PathVariable String assistantId, 
            @RequestParam AssistantConfiguration.Status status) {
        try {
            log.info("Updating configuration status for assistant: {} to {}", assistantId, status);
            AssistantConfigurationDTO configuration = assistantConfigurationService.updateConfigurationStatus(assistantId, status);
            return ResponseEntity.ok(configuration);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating configuration status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{assistantId}/configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteConfiguration(@PathVariable String assistantId) {
        try {
            log.info("Deleting configuration for assistant: {} (admin)", assistantId);
            assistantConfigurationService.deleteConfiguration(assistantId);
            return ResponseEntity.ok(Map.of("message", "Configuration deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    // User endpoints for viewing their own assistant configurations
    /**
     * Get current user's assistant configurations
     * @param authentication Current user authentication
     * @return List of user's assistant configurations
     */
    @GetMapping("/my-configurations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssistantConfigurationDTO>> getMyConfigurations(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("User {} requesting their assistant configurations", userId);
            
            List<AssistantConfigurationDTO> configurations = assistantConfigurationService.getConfigurationsByClientId(userId);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            log.error("Error getting user configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Get current user's assistant configurations with pagination
     * @param page Page number (zero-based)
     * @param size Page size
     * @param includeDocuments Whether to include documents in response
     * @param authentication Current user authentication
     * @return Page of user's assistant configurations
     */
    @GetMapping("/my-configurations/paginated")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AssistantConfigurationDTO>> getMyConfigurationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean includeDocuments,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("User {} requesting their paginated assistant configurations - page: {}, size: {}, includeDocuments: {}", 
                    userId, page, size, includeDocuments);
            
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<AssistantConfigurationDTO> configurations = assistantConfigurationService.getConfigurationsByClientIdPaginated(
                    userId, pageable, includeDocuments);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            log.error("Error getting user paginated configurations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    /**
     * Get current user's assistant configurations by status
     * @param status Configuration status
     * @param authentication Current user authentication
     * @return List of user's assistant configurations with specified status
     */
    @GetMapping("/my-configurations/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AssistantConfigurationDTO>> getMyConfigurationsByStatus(
            @PathVariable AssistantConfiguration.Status status,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("User {} requesting their assistant configurations with status: {}", userId, status);
            
            List<AssistantConfigurationDTO> configurations = assistantConfigurationService.getConfigurationsByClientIdAndStatus(
                    userId, status);
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            log.error("Error getting user configurations by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Get current user's assistant configuration count
     * @param authentication Current user authentication
     * @return Count of user's assistant configurations
     */
    @GetMapping("/my-configurations/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyConfigurationCount(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("User {} requesting their assistant configuration count", userId);
            
            long count = assistantConfigurationService.getConfigurationCountByClientId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalConfigurations", count);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user configuration count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Get current user's assistant configuration count by status
     * @param authentication Current user authentication
     * @return Count of user's assistant configurations grouped by status
     */
    @GetMapping("/my-configurations/count-by-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyConfigurationCountByStatus(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("User {} requesting their assistant configuration count by status", userId);
            
            Map<String, Object> counts = assistantConfigurationService.getConfigurationCountByClientIdAndStatus(userId);
            counts.put("userId", userId);
            counts.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("Error getting user configuration count by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
} 