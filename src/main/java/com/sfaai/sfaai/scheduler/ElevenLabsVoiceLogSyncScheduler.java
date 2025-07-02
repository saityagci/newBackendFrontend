package com.sfaai.sfaai.scheduler;

import com.sfaai.sfaai.service.ElevenLabsVoiceLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatically syncing ElevenLabs voice logs
 * Runs on a configurable schedule to keep voice logs up to date
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsVoiceLogSyncScheduler {

    private final ElevenLabsVoiceLogService elevenLabsVoiceLogService;

    @Value("${elevenlabs.voice-logs.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${elevenlabs.voice-logs.sync.cron:0 */30 * * * *}")
    private String syncCron;

    /**
     * Scheduled task to sync ElevenLabs voice logs
     * Runs every 30 minutes by default, configurable via properties
     */
    @Scheduled(cron = "${elevenlabs.voice-logs.sync.cron:0 */30 * * * *}")
    public void syncElevenLabsVoiceLogs() {
        if (!syncEnabled) {
            log.debug("ElevenLabs voice logs sync is disabled, skipping scheduled sync");
            return;
        }

        log.info("Starting scheduled ElevenLabs voice logs sync");
        
        try {
            ElevenLabsVoiceLogService.SyncSummary summary = elevenLabsVoiceLogService.syncVoiceLogsFromElevenLabs();
            
            log.info("Scheduled ElevenLabs voice logs sync completed: {} fetched, {} updated, {} skipped, {} errors, {} ms",
                    summary.fetched, summary.updated, summary.skipped, summary.errors, summary.durationMs);
            
            if (summary.errors > 0) {
                log.warn("Scheduled sync completed with {} errors", summary.errors);
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled ElevenLabs voice logs sync", e);
        }
    }
} 