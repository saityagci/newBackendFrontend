package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.service.AgentService;
import com.sfaai.sfaai.service.AgentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    // Create a new agent
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AgentDTO> create(@RequestBody AgentDTO dto) {
        AgentDTO created = agentService.createAgent(dto);
        return ResponseEntity.status(201).body(created); // 201 Created
    }

    // Get agent by ID
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<AgentDTO> getById(@PathVariable Long id) {
        AgentDTO agent = agentService.getAgent(id);
        if (agent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agent); // 200 OK
    }

    // Get all agents
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<AgentDTO>> getAll() {
        List<AgentDTO> agents = agentService.getAllAgents();
        return ResponseEntity.ok(agents); // 200 OK
    }

    // Update agent
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AgentDTO> update(@PathVariable Long id, @RequestBody AgentDTO dto) {
        AgentDTO updated = agentService.updateAgent(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated); // 200 OK
    }

    // Delete agent
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}