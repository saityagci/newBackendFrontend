package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voicelogs")
@RequiredArgsConstructor
public class VoiceLogController {

    private final VoiceLogService voiceLogService;

    // Create/save a voicelog (used by webhook or app)
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<VoiceLogDTO> save(@RequestBody VoiceLogDTO dto) {
        VoiceLogDTO saved = voiceLogService.save(dto);
        return ResponseEntity.status(201).body(saved);
    }

    // Get all voicelogs
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<VoiceLogDTO>> findAll() {
        return ResponseEntity.ok(voiceLogService.findAll());
    }

    // Get voicelog by ID
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<VoiceLogDTO> findById(@PathVariable Long id) {
        VoiceLogDTO dto = voiceLogService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // Get voicelogs for a specific agent
   @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<VoiceLogDTO>> findByAgentId(@PathVariable Long agentId) {
        return ResponseEntity.ok(voiceLogService.findByAgentId(agentId));
    }

    // Get voicelogs for a specific client
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VoiceLogDTO>> findByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(voiceLogService.findByClientId(clientId));
    }

    // Optionally: Delete voicelog
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voiceLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // No auth for webhook (add auth later if needed)
    @PostMapping("/webhook")
    public ResponseEntity<?> receiveVoiceLogWebhook(@RequestBody Map<String, Object> payload) {
        // Map payload to VoiceLogDTO (write a small helper if needed)
        VoiceLogDTO dto = new VoiceLogDTO();
        dto.setTranscript((String) payload.get("transcript"));
        dto.setAudioUrl((String) payload.get("audio_url"));
        dto.setProvider((String) payload.get("provider"));
        // ... any other mapping needed

        // Save to DB
        voiceLogService.save(dto);

        return ResponseEntity.ok("ok");
    }
}