package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voice-logs")
@RequiredArgsConstructor
public class VoiceLogController {

    private final VoiceLogService voiceLogService;

    @PostMapping
    public ResponseEntity<VoiceLogDTO> receiveLog(@RequestBody VoiceLogDTO dto) {
        return ResponseEntity.ok(voiceLogService.save(dto));
    }

    @GetMapping
    public ResponseEntity<List<VoiceLogDTO>> allLogs() {
        return ResponseEntity.ok(voiceLogService.findAll());
    }
}