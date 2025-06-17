package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.WorkflowLogCreateDTO;
import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.service.WorkflowLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;

@RestController
@RequestMapping("/api/workflowlogs")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WorkflowLogController {

    private final WorkflowLogService workflowLogService;
    private final ClientRepository clientRepository;

    // Create/save a workflow log (used by webhook or app)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping
    public ResponseEntity<WorkflowLogDTO> save(@Valid @RequestBody WorkflowLogCreateDTO dto) {
        log.debug("Creating new workflow log for agent: {}, client: {}", dto.getAgentId(), dto.getClientId());
        WorkflowLogDTO saved = workflowLogService.createWorkflowLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all workflow logs (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<WorkflowLogDTO>> findAll() {
        log.debug("Retrieving all workflow logs");
        return ResponseEntity.ok(workflowLogService.getAllWorkflowLogs());
    }

    // Get workflow log by ID
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowLogDTO> findById(@PathVariable @Positive Long id) {
        log.debug("Retrieving workflow log with id: {}", id);
        WorkflowLogDTO dto = workflowLogService.getWorkflowLogById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // Get workflow logs for a specific agent
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByAgentId(@PathVariable @Positive Long agentId) {
        log.debug("Retrieving workflow logs for agent: {}", agentId);
        List<WorkflowLogDTO> logs = workflowLogService.getWorkflowLogsByAgentId(agentId);
        log.debug("Found {} workflow logs for agent: {}", logs.size(), agentId);
        return ResponseEntity.ok(logs);
    }

    // Get workflow logs for a specific client
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByClientId(@PathVariable @Positive Long clientId) {
        log.debug("Retrieving workflow logs for client: {}", clientId);

        // Check if user has access to this client
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("User {} has authorities: {}", auth.getName(), 
                auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(java.util.stream.Collectors.joining(", ")));
        }

        if (auth != null && !auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            // If not admin, check if user is accessing their own data
            Optional<Client> client = clientRepository.findById(clientId);
            if (client.isEmpty() || !auth.getName().equals(client.get().getEmail())) {
                log.warn("Access denied: User {} attempted to access client {}", auth.getName(), clientId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(workflowLogService.getWorkflowLogsByClientId(clientId));
    }

    // Get workflow logs for a specific voice log
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/voicelog/{voiceLogId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByVoiceLogId(@PathVariable @Positive Long voiceLogId) {
        log.debug("Retrieving workflow logs for voice log: {}", voiceLogId);
        return ResponseEntity.ok(workflowLogService.getWorkflowLogsByVoiceLogId(voiceLogId));
    }

    // Delete workflow log
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        log.debug("Deleting workflow log with id: {}", id);
        workflowLogService.deleteWorkflowLog(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handle validation exceptions
     * @param ex The exception
     * @return Bad request response with error details
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + ex.getMessage());
    }
}