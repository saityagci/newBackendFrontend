package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.service.WorkflowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflowlogs")
@RequiredArgsConstructor
public class WorkflowLogController {

    private final WorkflowLogService workflowLogService;

    // Create/save a workflow log (used by webhook or app)
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<WorkflowLogDTO> save(@RequestBody WorkflowLogDTO dto) {
        WorkflowLogDTO saved = workflowLogService.save(dto);
        return ResponseEntity.status(201).body(saved);
    }

    // Get all workflow logs (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<WorkflowLogDTO>> findAll() {
        return ResponseEntity.ok(workflowLogService.findAll());
    }

    // Get workflow log by ID
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowLogDTO> findById(@PathVariable Long id) {
        WorkflowLogDTO dto = workflowLogService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // Get workflow logs for a specific agent
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByAgentId(@PathVariable Long agentId) {
        return ResponseEntity.ok(workflowLogService.findByAgentId(agentId));
    }

    // Get workflow logs for a specific client
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(workflowLogService.findByClientId(clientId));
    }

    // Get workflow logs for a specific voice log
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/voicelog/{voiceLogId}")
    public ResponseEntity<List<WorkflowLogDTO>> findByVoiceLogId(@PathVariable Long voiceLogId) {
        return ResponseEntity.ok(workflowLogService.findByVoiceLogId(voiceLogId));
    }

    // Optionally: Delete workflow log
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workflowLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}