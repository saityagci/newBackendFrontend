package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.SyncStatus;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.mapper.VapiAssistantMapper;
import com.sfaai.sfaai.repository.SyncStatusRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of VapiAssistantSyncService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VapiAssistantSyncServiceImpl implements VapiAssistantSyncService {

    private final VapiAgentService vapiAgentService;
    private final VapiAssistantRepository vapiAssistantRepository;
    private final VapiAssistantMapper vapiAssistantMapper;
    private final SyncStatusRepository syncStatusRepository;
    private final JdbcTemplate jdbcTemplate;
    private final com.sfaai.sfaai.util.FirstMessageFallbackAdapter firstMessageFallbackAdapter;

    /**
     * Synchronize all assistants from Vapi API to local database
     * @return Number of assistants synchronized
     */
    @Override
    @Transactional
    public int synchronizeAllAssistants() {
        log.info("Starting synchronization of all Vapi assistants");
        int syncCount = 0;
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        Exception lastException = null;

        // Create sync status entry
        SyncStatus syncStatus = SyncStatus.builder()
                .syncType("VAPI_ASSISTANTS")
                .startTime(LocalDateTime.now())
                .success(false) // Will update to true if successful
                .build();

        log.debug("Creating new sync status record with ID: {}", syncStatus.getId());
        try {
            // Save initial sync status
            syncStatusRepository.save(syncStatus);
            log.debug("Successfully saved initial sync status record with ID: {}", syncStatus.getId());
        } catch (Exception e) {
            log.error("Failed to save initial sync status: {}", e.getMessage(), e);
            // Continue with sync even if status record fails
        }

        while (!success && retryCount < maxRetries) {
            try {
                if (retryCount > 0) {
                    log.info("Retry attempt {} of {} for Vapi assistant synchronization", retryCount, maxRetries);
                    // Exponential backoff: 2s, 4s, 8s
                    Thread.sleep(2000L * (1L << (retryCount - 1)));
                }

                log.debug("Before API call to get all assistants");
                // Get all assistants from Vapi API
                VapiListAssistantsResponse response = null;
                try {
                    response = vapiAgentService.getAllAssistants();
                    log.debug("API call completed successfully");
                } catch (Exception e) {
                    log.error("Exception during API call to get assistants: {}", e.getMessage(), e);
                    throw e; // Rethrow to be handled by outer try-catch
                }

                if (response == null) {
                    log.error("API returned null response");
                    throw new IllegalStateException("API returned null response");
                }

                List<VapiAssistantDTO> assistants = response.getAssistants();
                log.debug("Response assistants list: {}", assistants);

                if (assistants == null || assistants.isEmpty()) {
                    log.warn("No assistants found in Vapi API response");
                    return 0;
                }

                log.info("Retrieved {} assistants from Vapi API", assistants.size());
                // Log first assistant details for debugging
                if (!assistants.isEmpty()) {
                    VapiAssistantDTO first = assistants.get(0);
                    log.debug("First assistant example - ID: {}, Name: {}, Status: {}, FirstMessage: {}", 
                        first.getAssistantId(), first.getName(), first.getStatus(), first.getFirstMessage());
                }

                log.debug("Extracting assistant IDs from API response");
                // Get all existing assistants for batch comparison
                List<String> apiAssistantIds = assistants.stream()
                        .map(VapiAssistantDTO::getAssistantId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                log.debug("Found {} unique assistant IDs from API", apiAssistantIds.size());
                if (!apiAssistantIds.isEmpty()) {
                    log.debug("First few assistant IDs: {}", 
                        apiAssistantIds.subList(0, Math.min(3, apiAssistantIds.size())));
                }

                log.debug("Before database query to find existing assistants");
                List<VapiAssistant> existingAssistants;
                try {
                    existingAssistants = vapiAssistantRepository.findByAssistantIdIn(apiAssistantIds);
                    log.debug("Found {} existing assistants in database", existingAssistants.size());
                } catch (Exception e) {
                    log.error("Database error when querying existing assistants: {}", e.getMessage(), e);
                    throw e; // Rethrow to be handled by outer try-catch
                }

                Map<String, VapiAssistant> existingAssistantsMap = existingAssistants.stream()
                        .collect(Collectors.toMap(VapiAssistant::getAssistantId, Function.identity()));
                log.debug("Created map of {} existing assistants by ID", existingAssistantsMap.size());

                // Process each assistant
                List<VapiAssistant> entitiesToSave = new ArrayList<>();

                for (VapiAssistantDTO dto : assistants) {
                    if (dto.getAssistantId() == null) {
                        log.warn("Skipping assistant with null ID");
                        continue;
                    }

                    // Check if assistant already exists in database
                    VapiAssistant entity = existingAssistantsMap.get(dto.getAssistantId());

                    if (entity != null) {
                        // Update existing entity
                        log.debug("Updating existing assistant: ID={}, Name={}", entity.getAssistantId(), entity.getName());
                        try {
                            updateEntityFromDto(entity, dto);
                            log.debug("Successfully updated existing assistant: {}", entity.getAssistantId());
                        } catch (Exception e) {
                            log.error("Error updating assistant {}: {}", entity.getAssistantId(), e.getMessage(), e);
                            throw e; // Rethrow to be handled by outer try-catch
                        }
                    } else {
                        // Create new entity
                        log.debug("Creating new assistant entity for ID: {}", dto.getAssistantId());
                        try {
                            entity = vapiAssistantMapper.toEntity(dto);
                            if (entity == null) {
                                log.error("Mapper returned null entity for assistant ID: {}", dto.getAssistantId());
                                continue; // Skip this one but continue processing others
                            }
                            entity.setLastSyncedAt(LocalDateTime.now());
                            entity.setSyncStatus("SUCCESS");
                            log.debug("Successfully created new assistant entity: ID={}, Name={}", 
                                    entity.getAssistantId(), entity.getName());
                        } catch (Exception e) {
                            log.error("Error mapping assistant {}: {}", dto.getAssistantId(), e.getMessage(), e);
                            continue; // Skip this one but continue processing others
                        }
                    }

                    entitiesToSave.add(entity);
                    syncCount++;
                }

                // Save all assistants in batch
                if (!entitiesToSave.isEmpty()) {
                    log.debug("Preparing to save {} assistants to database", entitiesToSave.size());
                    // Log details of first few assistants for debugging
                    int logCount = Math.min(3, entitiesToSave.size());
                    for (int i = 0; i < logCount; i++) {
                        VapiAssistant entity = entitiesToSave.get(i);
                        log.debug("Assistant to save #{}: ID={}, Name={}, Status={}", 
                                i+1, entity.getAssistantId(), entity.getName(), entity.getStatus());

                        // Explicitly check and log the problematic fields
                        log.debug("  - firstMessage: {}", entity.getFirstMessage());
                        log.debug("  - voiceProvider: {}", entity.getVoiceProvider());
                        log.debug("  - voiceId: {}", entity.getVoiceId());

                        // Force values if they're still missing (debug step)
                        if (entity.getFirstMessage() == null) {
                            // Find the original DTO
                            assistants.stream()
                                .filter(dto -> dto.getAssistantId().equals(entity.getAssistantId()))
                                .findFirst()
                                .ifPresent(dto -> {
                                    log.debug("DTO firstMessage: {}", dto.getFirstMessage());
                                    if (dto.getFirstMessage() != null) {
                                        entity.setFirstMessage(dto.getFirstMessage());
                                        log.debug("Forced firstMessage from DTO: {}", dto.getFirstMessage());
                                    }
                                });
                        }
                    }

                    try {
                        log.debug("Executing saveAll operation for {} entities", entitiesToSave.size());
                        List<VapiAssistant> savedEntities = vapiAssistantRepository.saveAll(entitiesToSave);
                        log.debug("SaveAll operation completed, returned {} entities", 
                                savedEntities != null ? savedEntities.size() : 0);
                        log.info("Successfully saved {} assistants to database", entitiesToSave.size());

                        // Verify entities were actually saved
                        try {
                            log.debug("Verifying assistants were saved by querying first assistant");
                            if (!entitiesToSave.isEmpty()) {
                                String idToCheck = entitiesToSave.get(0).getAssistantId();
                                Optional<VapiAssistant> checkResult = vapiAssistantRepository.findById(idToCheck);
                                log.debug("Verification query for ID {} returned: {}", 
                                        idToCheck, checkResult.isPresent() ? "entity found" : "no entity found");

                                if (checkResult.isPresent()) {
                                    VapiAssistant saved = checkResult.get();
                                    log.debug("Saved assistant firstMessage value: {}", saved.getFirstMessage());

                                    // If firstMessage is still null, try a direct database update as a last resort
                                    if (saved.getFirstMessage() == null) {
                                        try {
                                            log.debug("Attempting direct database update for firstMessage");
                                            // Find the original DTO with this ID
                                            for (VapiAssistantDTO dto : assistants) {
                                                if (dto.getAssistantId().equals(idToCheck) && dto.getFirstMessage() != null) {
                                                    saved.setFirstMessage(dto.getFirstMessage());
                                                    vapiAssistantRepository.save(saved);
                                                    log.debug("Direct update of firstMessage successful: {}", dto.getFirstMessage());
                                                    break;
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.error("Error during direct update: {}", e.getMessage(), e);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error during verification query: {}", e.getMessage());
                            // Don't rethrow - this is just for debugging
                        }

                        success = true;
                    } catch (DataAccessException e) {
                        log.error("Database error while saving assistants: {}", e.getMessage(), e);
                        log.error("SQL error details: {}", e.getMostSpecificCause().getMessage());
                        // Mark assistants as failed
                        for (VapiAssistant entity : entitiesToSave) {
                            entity.setSyncStatus("DB_ERROR");
                        }
                        throw e;
                    } catch (Exception e) {
                        log.error("Unexpected error during save operation: {}", e.getMessage(), e);
                        throw e;
                    }
                } else {
                    log.warn("No assistants to save to database");
                    success = true;
                }

            } catch (InterruptedException e) {
                log.warn("Sync operation interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("Error synchronizing Vapi assistants after {} retries: {}", maxRetries, e.getMessage(), e);
                } else {
                    log.warn("Error during Vapi assistant sync (will retry): {}", e.getMessage());
                }
            }
        }

        if (!success && lastException != null) {
            // All retries failed
            // Update sync status
            syncStatus.setEndTime(LocalDateTime.now());
            syncStatus.setSuccess(false);
            syncStatus.setMessage("Failed to synchronize after " + maxRetries + " attempts");
            syncStatus.setErrorDetails(lastException.getMessage());
            syncStatusRepository.save(syncStatus);

            throw new RuntimeException("Failed to synchronize Vapi assistants after " + maxRetries + " attempts", lastException);
        }

        // Direct SQL update for any assistants missing firstMessage
        try {
            log.debug("Performing direct SQL update for any missing firstMessage values");

            // Get all assistants from Vapi API to ensure we have the latest data
            VapiListAssistantsResponse response = vapiAgentService.getAllAssistants();
            List<VapiAssistantDTO> apiAssistants = response.getAssistants();

            if (apiAssistants != null && !apiAssistants.isEmpty()) {
                log.debug("Got {} assistants from API for direct SQL update", apiAssistants.size());

                for (VapiAssistantDTO dto : apiAssistants) {
                    if (dto.getFirstMessage() != null) {
                        String sql = "UPDATE vapi_assistant SET first_message = ? WHERE assistant_id = ?"; 
                        int updated = jdbcTemplate.update(sql, dto.getFirstMessage(), dto.getAssistantId());
                        if (updated > 0) {
                            log.debug("Direct SQL update successful for assistant {}: firstMessage = {}", 
                                    dto.getAssistantId(), dto.getFirstMessage());
                        }
                    }

                    // Also update voice_id if available
                    if (dto.getVoice() != null && dto.getVoice().getVoiceId() != null) {
                        String sql = "UPDATE vapi_assistant SET voice_id = ?, voice_provider = ? WHERE assistant_id = ?"; 
                        int updated = jdbcTemplate.update(sql, dto.getVoice().getVoiceId(), 
                                dto.getVoice().getProvider(), dto.getAssistantId());
                        if (updated > 0) {
                            log.debug("Direct SQL update successful for assistant {}: voice_id = {}", 
                                    dto.getAssistantId(), dto.getVoice().getVoiceId());
                        }
                    }
                }
            } else {
                log.warn("No assistants returned from API for direct SQL update");
            }
        } catch (Exception e) {
            log.error("Error during direct SQL update: {}", e.getMessage(), e);
            // Continue with the process - this is just a final attempt
        }

        // Update sync status
        syncStatus.setEndTime(LocalDateTime.now());
        syncStatus.setSuccess(true);
        syncStatus.setItemsProcessed(syncCount);
        syncStatus.setMessage("Successfully synchronized " + syncCount + " Vapi assistants");
        syncStatusRepository.save(syncStatus);

        log.info("Completed synchronization of Vapi assistants. Synced {} assistants", syncCount);
        return syncCount;
    }

    /**
     * Synchronize a specific assistant from Vapi API to local database
     * @param assistantId Assistant ID to sync
     * @return true if successful, false otherwise
     */
    @Override
    @Transactional
    public boolean synchronizeAssistant(String assistantId) {
        log.info("Synchronizing Vapi assistant with ID: {}", assistantId);

        // Create sync status entry
        SyncStatus syncStatus = SyncStatus.builder()
                .syncType("VAPI_ASSISTANT_SINGLE")
                .startTime(LocalDateTime.now())
                .success(false) // Will update to true if successful
                .message("Syncing assistant: " + assistantId)
                .build();

        // Save initial sync status
        syncStatusRepository.save(syncStatus);

        try {
            // Get all assistants and find the matching one
            // (Using getAllAssistants since Vapi doesn't seem to have a get-by-id endpoint)
            VapiListAssistantsResponse response = vapiAgentService.getAllAssistants();
            List<VapiAssistantDTO> assistants = response.getAssistants();

            if (assistants == null || assistants.isEmpty()) {
                log.warn("No assistants found in Vapi API response");
                return false;
            }

            // Find the assistant with the matching ID
            VapiAssistantDTO targetAssistant = assistants.stream()
                    .filter(a -> assistantId.equals(a.getAssistantId()))
                    .findFirst()
                    .orElse(null);

            if (targetAssistant == null) {
                log.warn("Assistant with ID {} not found in Vapi API", assistantId);
                return false;
            }

            // Check if assistant already exists in database
            Optional<VapiAssistant> existingAssistant = 
                    vapiAssistantRepository.findById(assistantId);

            VapiAssistant entity;
            if (existingAssistant.isPresent()) {
                // Update existing entity
                entity = existingAssistant.get();
                updateEntityFromDto(entity, targetAssistant);
                log.debug("Updated existing assistant: {}", entity.getAssistantId());
            } else {
                // Create new entity
                entity = vapiAssistantMapper.toEntity(targetAssistant);
                log.debug("Created new assistant: {}", entity.getAssistantId());
            }

            // Save the entity
            vapiAssistantRepository.save(entity);
            log.info("Successfully synchronized assistant with ID: {}", assistantId);

            // Update sync status
            syncStatus.setEndTime(LocalDateTime.now());
            syncStatus.setSuccess(true);
            syncStatus.setItemsProcessed(1);
            syncStatus.setMessage("Successfully synchronized assistant: " + assistantId);
            syncStatusRepository.save(syncStatus);

            return true;

        } catch (Exception e) {
            log.error("Error synchronizing Vapi assistant {}: {}", assistantId, e.getMessage(), e);

            // Update sync status
            syncStatus.setEndTime(LocalDateTime.now());
            syncStatus.setSuccess(false);
            syncStatus.setMessage("Failed to synchronize assistant: " + assistantId);
            syncStatus.setErrorDetails(e.getMessage());
            syncStatusRepository.save(syncStatus);

            return false;
        }
    }

    /**
     * Update entity fields from DTO without replacing the entire entity
     * @param entity Entity to update
     * @param dto DTO containing the new data
     */
    private void updateEntityFromDto(VapiAssistant entity, VapiAssistantDTO dto) {
        // Update basic fields
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());

        // Debug and fix firstMessage field
        String firstMessage = dto.getFirstMessage();
        log.debug("Setting firstMessage for assistant {}: {}", dto.getAssistantId(), firstMessage);
        entity.setFirstMessage(firstMessage);

        // Apply fallback if firstMessage is still null
        if (entity.getFirstMessage() == null) {
            log.debug("FirstMessage is null after mapping, applying fallback");
            firstMessageFallbackAdapter.applyFallbackMessage(entity);
            log.debug("Applied fallback firstMessage: {}", entity.getFirstMessage());
        }

        // Update voice info with null safety
        if (dto.getVoice() != null) {
            if (dto.getVoice().getProvider() != null) entity.setVoiceProvider(dto.getVoice().getProvider());
            if (dto.getVoice().getVoiceId() != null) entity.setVoiceId(dto.getVoice().getVoiceId());
        }

        // Update model info with null safety
        if (dto.getModel() != null) {
            if (dto.getModel().getProvider() != null) entity.setModelProvider(dto.getModel().getProvider());
            if (dto.getModel().getModel() != null) entity.setModelName(dto.getModel().getModel());
        }

        // Update transcriber info with null safety
        if (dto.getTranscriber() != null) {
            if (dto.getTranscriber().getProvider() != null) entity.setTranscriberProvider(dto.getTranscriber().getProvider());
            if (dto.getTranscriber().getModel() != null) entity.setTranscriberModel(dto.getTranscriber().getModel());
            if (dto.getTranscriber().getLanguage() != null) entity.setTranscriberLanguage(dto.getTranscriber().getLanguage());
        }

        // Update sync metadata
        entity.setLastSyncedAt(LocalDateTime.now());
        entity.setSyncStatus("SUCCESS");
    }
}
