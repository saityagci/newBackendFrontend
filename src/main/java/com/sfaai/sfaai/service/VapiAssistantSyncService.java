package com.sfaai.sfaai.service;
/**
 * Service for synchronizing Vapi assistants with local database
 */
public interface VapiAssistantSyncService {

    /**
     * Synchronize all assistants from Vapi API to local database
     * @return Number of assistants synchronized
     */
    int synchronizeAllAssistants();

    /**
     * Synchronize a specific assistant from Vapi API to local database
     * @param assistantId Assistant ID to sync
     * @return true if successful, false otherwise
     */
    boolean synchronizeAssistant(String assistantId);
}
