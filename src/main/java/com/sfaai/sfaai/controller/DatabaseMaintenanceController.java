package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.util.DatabaseUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for database maintenance operations
 */
@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
public class DatabaseMaintenanceController {

    private final DatabaseUpdater databaseUpdater;

    /**
     * Fix audio URLs in the database
     * 
     * @param pattern Optional specific pattern to fix (defaults to common patterns)
     * @return Number of records updated
     */
    @PostMapping("/fix-audio-urls")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> fixAudioUrls(
            @RequestParam(required = false) String pattern) {

        int updated;
        if (pattern != null && !pattern.isEmpty()) {
            updated = databaseUpdater.updateAudioUrls(pattern);
        } else {
            updated = databaseUpdater.fixCommonUrlPatterns();
        }

        return ResponseEntity.ok("Updated " + updated + " records");
    }
}
