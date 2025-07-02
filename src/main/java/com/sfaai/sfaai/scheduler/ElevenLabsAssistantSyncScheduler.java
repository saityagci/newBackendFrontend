package com.sfaai.sfaai.scheduler;

import com.sfaai.sfaai.service.ElevenLabsAssistantService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsAssistantSyncScheduler {

    private final ElevenLabsAssistantService elevenLabsAssistantService;

    @Value("${elevenlabs.sync.interval:600000}")
    private long syncInterval;

    @PostConstruct
    public void validateServices() {
        log.debug("Validating ElevenLabsAssistantService injection: {}",
                elevenLabsAssistantService != null ? "Service injected successfully" : "Service injection FAILED");
        if (elevenLabsAssistantService == null) {
            log.error("CRITICAL ERROR: ElevenLabsAssistantService is null! Scheduled syncs will fail.");
        } else {
            log.debug("ElevenLabsAssistantService class: {}", elevenLabsAssistantService.getClass().getName());
        }
    }

    /**
     * Initial sync on application startup
     * Uses a separate thread to avoid blocking application startup
     */
    @PostConstruct
    public void initialSync() {
        log.info("Scheduling initial ElevenLabs assistant synchronization after startup");
        Thread initialSyncThread = new Thread(() -> {
            try {
                Thread.sleep(10000); // Wait 10 seconds for app to fully initialize
                log.info("Performing initial ElevenLabs assistant synchronization");
                int syncCount = elevenLabsAssistantService.syncAllAssistants();
                log.info("Initial sync completed. Synchronized {} assistants", syncCount);
            } catch (InterruptedException e) {
                log.warn("Initial sync thread interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error during initial ElevenLabs assistant sync: {}", e.getMessage(), e);
            }
        });
        initialSyncThread.setName("initial-elevenlabs-sync");
        initialSyncThread.setDaemon(true);
        initialSyncThread.start();
    }

    /**
     * Scheduled sync every 10 minutes (or as configured in properties)
     * Uses fixedDelayString to ensure the previous execution completes before the next one starts
     */
    @Scheduled(fixedDelayString = "${elevenlabs.sync.interval:600000}", initialDelay = 600000)
    public void scheduledSync() {
        String now = java.time.ZonedDateTime.now().toString();
        log.info("ElevenLabs scheduled sync running at {}", now);
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        while (!success && retryCount < maxRetries) {
            try {
                if (retryCount > 0) {
                    log.info("Retry attempt {} of {}", retryCount, maxRetries);
                    Thread.sleep(5000L * (1L << (retryCount - 1)));
                }
                int fetched = 0;
                int updated = 0;
                try {
                    // Fetch count for logging
                    com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse apiAssistants = elevenLabsAssistantService.getAllAssistantsFromApi();
                    if (apiAssistants != null && apiAssistants.getAssistants() != null) {
                        fetched = apiAssistants.getAssistants().size();
                    }
                } catch (Exception e) {
                    log.error("Error fetching assistants from ElevenLabs API", e);
                }
                updated = elevenLabsAssistantService.syncAllAssistants();
                log.info("ElevenLabs sync: {} assistants fetched from API, {} saved/updated", fetched, updated);
                if (updated == 0) {
                    log.info("No assistants updated; all records up-to-date");
                }
                success = true;
            } catch (Exception e) {
                retryCount++;
                log.error("Exception during scheduled ElevenLabs assistant sync (attempt {}):", retryCount, e);
                if (retryCount >= maxRetries) {
                    log.error("Error during scheduled ElevenLabs assistant sync after {} retries: {}", maxRetries, e.getMessage(), e);
                } else {
                    log.warn("Error during scheduled ElevenLabs assistant sync (will retry): {}", e.getMessage());
                }
            }
        }
    }
} 