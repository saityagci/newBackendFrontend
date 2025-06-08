package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.service.WorkflowLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/workflow-logs")
@RequiredArgsConstructor
public class WorkflowLogController {

    private final WorkflowLogService workflowLogService;

    @PostMapping
    public ResponseEntity<Void> receiveLog(@RequestBody WorkflowLogDTO dto) {
        workflowLogService.save(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Optional<WorkflowLogDTO>> getLogs(@PathVariable Long clientId) {
        return ResponseEntity.ok(workflowLogService.findByClient(String.valueOf(clientId)));
    }
}