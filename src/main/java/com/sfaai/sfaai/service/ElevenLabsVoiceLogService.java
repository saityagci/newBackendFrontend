package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VoiceLogDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing ElevenLabs voice logs
 * Handles synchronization from ElevenLabs API and audio streaming
 */
public interface ElevenLabsVoiceLogService {

    /**
     * Get all ElevenLabs voice logs from the database
     * @return List of ElevenLabs voice log DTOs
     */
    List<VoiceLogDTO> getAllElevenLabsVoiceLogs();

    /**
     * Get ElevenLabs voice logs with pagination
     * @param pageable Pagination information
     * @return Page of ElevenLabs voice log DTOs
     */
    Page<VoiceLogDTO> getElevenLabsVoiceLogs(Pageable pageable);

    /**
     * Get a specific ElevenLabs voice log by external call ID
     * @param externalCallId The ElevenLabs conversation ID
     * @return Voice log DTO or null if not found
     */
    VoiceLogDTO getElevenLabsVoiceLogByExternalCallId(String externalCallId);

    /**
     * Stream/download audio for a specific ElevenLabs voice log
     * @param externalCallId The ElevenLabs conversation ID
     * @return Audio resource or null if not found
     */
    Resource getElevenLabsVoiceLogAudio(String externalCallId);

    /**
     * Synchronize voice logs from ElevenLabs API
     * Fetches all conversations and updates the local database
     * @return Sync summary with counts of fetched, updated, and error records
     */
    SyncSummary syncVoiceLogsFromElevenLabs();

    /**
     * Manual sync trigger for testing
     * @return Sync summary
     */
    SyncSummary manualSync();

    /**
     * Force update transcript format for all existing ElevenLabs voice logs
     * Converts [agent] to AI: and [user] to User:
     */
    SyncSummary forceUpdateTranscriptFormat();

    /**
     * Summary of sync operation results
     */
    class SyncSummary {
        public final int fetched;
        public final int updated;
        public final int skipped;
        public final int errors;
        public final long durationMs;
        public final List<String> updatedIds;
        public final List<String> skippedIds;
        public final List<String> errorIds;

        public SyncSummary(int fetched, int updated, int skipped, int errors, 
                          long durationMs, List<String> updatedIds, 
                          List<String> skippedIds, List<String> errorIds) {
            this.fetched = fetched;
            this.updated = updated;
            this.skipped = skipped;
            this.errors = errors;
            this.durationMs = durationMs;
            this.updatedIds = updatedIds;
            this.skippedIds = skippedIds;
            this.errorIds = errorIds;
        }
    }
} 