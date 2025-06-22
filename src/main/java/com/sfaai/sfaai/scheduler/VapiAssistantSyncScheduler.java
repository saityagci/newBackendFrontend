package com.sfaai.sfaai.scheduler;

import com.sfaai.sfaai.service.VapiAssistantSyncService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


/**
 * Scheduler for syncing Vapi assistants with local database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VapiAssistantSyncScheduler {

    private final VapiAssistantSyncService vapiAssistantSyncService;

    @Value("${vapi.sync.interval:600000}")
    private long syncInterval;

    @PostConstruct
    public void validateServices() {
        log.debug("Validating VapiAssistantSyncService injection: {}", 
                vapiAssistantSyncService != null ? "Service injected successfully" : "Service injection FAILED");
        if (vapiAssistantSyncService == null) {
            log.error("CRITICAL ERROR: VapiAssistantSyncService is null! Scheduled syncs will fail.");
        } else {
            log.debug("VapiAssistantSyncService class: {}", vapiAssistantSyncService.getClass().getName());
        }
    }

    /**
     * Initial sync on application startup
     * Uses a separate thread to avoid blocking application startup
     */
    @PostConstruct
    public void initialSync() {
        log.info("Scheduling initial Vapi assistant synchronization after startup");

        // Run in a separate thread after a short delay to not block application startup
        // This ensures all dependencies are fully initialized
        Thread initialSyncThread = new Thread(() -> {
            try {
                // Wait 10 seconds for application to fully initialize
                Thread.sleep(10000);
                log.info("Performing initial Vapi assistant synchronization");
                int syncCount = vapiAssistantSyncService.synchronizeAllAssistants();
                log.info("Initial sync completed. Synchronized {} assistants", syncCount);
            } catch (InterruptedException e) {
                log.warn("Initial sync thread interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error during initial assistant sync: {}", e.getMessage(), e);
                // Don't rethrow - we don't want to prevent application startup if sync fails
            }
        });

        initialSyncThread.setName("initial-vapi-sync");
        initialSyncThread.setDaemon(true);
        initialSyncThread.start();
    }

    /**
     * Scheduled sync every 10 minutes (or as configured in properties)
     * Uses fixedDelayString to ensure the previous execution completes before the next one starts
     */
    @Scheduled(fixedDelayString = "${vapi.sync.interval:600000}", initialDelay = 600000)
    public void scheduledSync() {
        log.info("Starting scheduled Vapi assistant synchronization (interval: {} ms)", syncInterval);

        // Simple retry mechanism
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (!success && retryCount < maxRetries) {
            try {
                if (retryCount > 0) {
                    log.info("Retry attempt {} of {}", retryCount, maxRetries);
                    // Exponential backoff: 5s, 10s, 20s
                    Thread.sleep(5000L * (1L << (retryCount - 1)));
                }

                int syncCount = vapiAssistantSyncService.synchronizeAllAssistants();
                log.info("Scheduled sync completed. Synchronized {} assistants", syncCount);
                success = true;
            } catch (InterruptedException e) {
                log.warn("Sync thread interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("Error during scheduled assistant sync after {} retries: {}", maxRetries, e.getMessage(), e);
                } else {
                    log.warn("Error during scheduled assistant sync (will retry): {}", e.getMessage());
                }
            }
        }
    }
}
