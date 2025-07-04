package com.sfaai.sfaai.service.impl;


import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.service.VoiceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VoiceLogServiceImpl implements VoiceLogService {

    private final VoiceLogMapper voiceLogMapper;
    private final VoiceLogRepository voiceLogRepository;
    private final VapiAssistantRepository vapiAssistantRepository;
    private final ElevenLabsAssistantRepository elevenLabsAssistantRepository;

    public VoiceLogServiceImpl(VoiceLogMapper voiceLogMapper, VoiceLogRepository voiceLogRepository, VapiAssistantRepository vapiAssistantRepository, ElevenLabsAssistantRepository elevenLabsAssistantRepository) {
        this.voiceLogMapper = voiceLogMapper;
        this.voiceLogRepository = voiceLogRepository;
        this.vapiAssistantRepository = vapiAssistantRepository;
        this.elevenLabsAssistantRepository = elevenLabsAssistantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceLogDTO getVoiceLogById(Long id) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));
        return voiceLogMapper.toDto(voiceLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getAllVoiceLogs() {
        List<VoiceLog> voiceLogs = voiceLogRepository.findAll();
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceLogDTO> getVoiceLogs(Pageable pageable) {
        Page<VoiceLog> voiceLogs = voiceLogRepository.findAll(pageable);
        return voiceLogs.map(voiceLogMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByClientId(Long clientId) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByClientId(clientId);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByAgentId(Long agentId) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByAgentId(agentId);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoiceLogDTO> getVoiceLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<VoiceLog> voiceLogs = voiceLogRepository.findByCreatedAtBetween(startDate, endDate);
        return voiceLogMapper.toDtoList(voiceLogs);
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceLogDTO getVoiceLogByExternalCallId(String externalCallId) {
        Optional<VoiceLog> voiceLogOpt = voiceLogRepository.findByExternalCallId(externalCallId);
        return voiceLogOpt.map(voiceLogMapper::toDto).orElse(null);
    }

    /**
     * Helper method to create a new voice log
     * Extracted to avoid code duplication
     *
     * @param dto The voice log create DTO
     * @return The created voice log DTO
     */
    @Transactional
    protected VoiceLogDTO createNewVoiceLog(VoiceLogCreateDTO dto) {
        // Log the durationMinutes value from the DTO
        log.debug("Creating new voice log with durationMinutes: {}", dto.getDurationMinutes());
        // Create a new voice log entity from the DTO
        VoiceLog voiceLog = voiceLogMapper.createEntityFromDto(dto);

        // Handle different providers
        VoiceLog.Provider provider = VoiceLog.Provider.valueOf(dto.getProvider().toUpperCase());
        
        if (provider == VoiceLog.Provider.VAPI) {
            // Handle Vapi assistant
            if (dto.getAssistantId() != null) {
                VapiAssistant assistant = vapiAssistantRepository.findById(dto.getAssistantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vapi Assistant not found: " + dto.getAssistantId()));
                voiceLog.setVapiAssistant(assistant);
            } else if (dto.getExternalAgentId() != null) {
                // Fallback to externalAgentId if assistantId is not provided
                VapiAssistant assistant = vapiAssistantRepository.findById(dto.getExternalAgentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vapi Assistant not found: " + dto.getExternalAgentId()));
                voiceLog.setVapiAssistant(assistant);
            } else {
                throw new IllegalArgumentException("Assistant ID must not be null for Vapi provider");
            }
        } else if (provider == VoiceLog.Provider.ELEVENLABS) {
            // Handle ElevenLabs assistant
            if (dto.getAssistantId() != null) {
                ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(dto.getAssistantId())
                        .orElseThrow(() -> new ResourceNotFoundException("ElevenLabs Assistant not found: " + dto.getAssistantId()));
                voiceLog.setElevenLabsAssistant(assistant);
            } else if (dto.getExternalAgentId() != null) {
                // Fallback to externalAgentId if assistantId is not provided
                ElevenLabsAssistant assistant = elevenLabsAssistantRepository.findById(dto.getExternalAgentId())
                        .orElseThrow(() -> new ResourceNotFoundException("ElevenLabs Assistant not found: " + dto.getExternalAgentId()));
                voiceLog.setElevenLabsAssistant(assistant);
            } else {
                throw new IllegalArgumentException("Assistant ID must not be null for ElevenLabs provider");
            }
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        VoiceLog savedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(savedVoiceLog);
    }

    /**
     * Update an existing voice log with new data from a webhook
     * Ensures all required fields (audioUrl, durationMinutes, startedAt, endedAt) are updated
     * 
     * @param existingLog The existing voice log entity
     * @param dto The new data from the webhook
     * @return Updated voice log DTO
     */
    @Transactional
    protected VoiceLogDTO updateVoiceLog(VoiceLog existingLog, VoiceLogCreateDTO dto) {
        // Log the incoming durationMinutes value
        log.debug("Updating voice log (id={}) with durationMinutes: {}", 
                existingLog.getId(), dto.getDurationMinutes());
        // Log the current state of the critical fields
        log.info("Updating voice log ID {} with new data. Current values - audioUrl: {}, durationMinutes: {}, startedAt: {}, endedAt: {}", 
                existingLog.getId(), 
                existingLog.getAudioUrl() != null ? "present" : "missing",
                existingLog.getDurationMinutes(),
                existingLog.getStartedAt(),
                existingLog.getEndedAt());

        log.info("New values from webhook - audioUrl: {}, durationMinutes: {}, startedAt: {}, endedAt: {}",
                dto.getAudioUrl() != null ? "present" : "missing",
                dto.getDurationMinutes(),
                dto.getStartedAt(),
                dto.getEndedAt());

        // Update all fields with the new values from the DTO

        // Update status
        if (dto.getStatus() != null) {
            log.debug("Updating status from {} to {}", existingLog.getStatus(), dto.getStatus());
            existingLog.setStatus(dto.getStatus());
        } else if (dto.getEndedAt() != null && 
                  (existingLog.getStatus() == VoiceLog.Status.INITIATED || 
                   existingLog.getStatus() == VoiceLog.Status.IN_PROGRESS || 
                   existingLog.getStatus() == VoiceLog.Status.RINGING)) {
            // If we have an end time and the call is not in a terminal state, mark it as completed
            log.debug("Call has ended, updating status from {} to COMPLETED", existingLog.getStatus());
            existingLog.setStatus(VoiceLog.Status.COMPLETED);
        }

        // Update timestamps if provided (never overwrite existing timestamps with null)
        if (dto.getStartedAt() != null) {
            log.debug("Updating startedAt from {} to {}", existingLog.getStartedAt(), dto.getStartedAt());
            existingLog.setStartedAt(dto.getStartedAt());
        }
        if (dto.getEndedAt() != null) {
            log.debug("Updating endedAt from {} to {}", existingLog.getEndedAt(), dto.getEndedAt());
            existingLog.setEndedAt(dto.getEndedAt());
        }

        // Explicitly set durationMinutes if provided
        if (dto.getDurationMinutes() != null) {
            log.debug("Setting durationMinutes to {} for voice log id={}", 
                    dto.getDurationMinutes(), existingLog.getId());
            existingLog.setDurationMinutes(dto.getDurationMinutes());
        } else if (dto.getStartedAt() != null && dto.getEndedAt() != null) {
            // Calculate durationMinutes from start/end times if not explicitly provided
            long seconds = java.time.Duration.between(dto.getStartedAt(), dto.getEndedAt()).getSeconds();
            double calculatedDurationMinutes = seconds / 60.0f;
            log.debug("Calculated durationMinutes as {} for voice log id={}", 
                    calculatedDurationMinutes, existingLog.getId());
            existingLog.setDurationMinutes(calculatedDurationMinutes);
        }

        // Update transcript only if it's not null or empty
        // If we already have a transcript, only update if the new one is better (longer)
        if (dto.getTranscript() != null && !dto.getTranscript().isEmpty()) {
            if (existingLog.getTranscript() == null || existingLog.getTranscript().isEmpty() ||
                dto.getTranscript().length() > existingLog.getTranscript().length()) {
                log.debug("Updating transcript (old length: {}, new length: {})", 
                        existingLog.getTranscript() != null ? existingLog.getTranscript().length() : 0, 
                        dto.getTranscript().length());
                existingLog.setTranscript(dto.getTranscript());
            }
        }

        // Update audio URL - ALWAYS check if the incoming webhook has a better URL
        if (dto.getAudioUrl() != null && !dto.getAudioUrl().isEmpty()) {
            boolean shouldUpdate = false;

            // If we don't have an audio URL yet, always update
            if (existingLog.getAudioUrl() == null || existingLog.getAudioUrl().isEmpty()) {
                shouldUpdate = true;
                log.debug("No existing audioUrl, using new one: {}", dto.getAudioUrl());
            } 
            // If the new URL is different and seems to be a recording URL, update
            else if (!existingLog.getAudioUrl().equals(dto.getAudioUrl())) {
                // Prefer URLs with "recording" or "audio" or file extensions in them
                boolean newUrlLooksLikeRecording = dto.getAudioUrl().contains("recording") || 
                                                 dto.getAudioUrl().contains("audio") ||
                                                 dto.getAudioUrl().endsWith(".mp3") ||
                                                 dto.getAudioUrl().endsWith(".wav") ||
                                                 dto.getAudioUrl().endsWith(".m4a");

                if (newUrlLooksLikeRecording) {
                    shouldUpdate = true;
                    log.debug("Found better audioUrl, updating from {} to {}", existingLog.getAudioUrl(), dto.getAudioUrl());
                }
            }

            if (shouldUpdate) {
                existingLog.setAudioUrl(dto.getAudioUrl());
                log.info("Updated audioUrl to: {}", dto.getAudioUrl());
            }
        } else {
            // Special case: If we don't have an audioUrl in the dto but we have raw payload data,
            // attempt to extract the URL directly here as a last resort
            if ((existingLog.getAudioUrl() == null || existingLog.getAudioUrl().isEmpty()) && 
                dto.getRawPayload() != null && !dto.getRawPayload().isEmpty()) {

                log.info("No audioUrl in the dto, attempting to extract from raw payload");
                String rawData = dto.getRawPayload();

                // Use our specialized utility class to extract the URL
                String extractedUrl = com.sfaai.sfaai.util.AudioUrlExtractor.extractFromJson(rawData);
                if (extractedUrl != null && !extractedUrl.isEmpty()) {
                    log.info("Successfully extracted audioUrl from raw payload: {}", extractedUrl);
                    existingLog.setAudioUrl(extractedUrl);
                } else {
                    log.warn("Could not extract audioUrl from raw payload");
                }
            }
        }

        // Update phone number if provided and we don't have one yet
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()) {
            if (existingLog.getPhoneNumber() == null || existingLog.getPhoneNumber().isEmpty()) {
                log.debug("Setting phoneNumber to: {}", dto.getPhoneNumber());
                existingLog.setPhoneNumber(dto.getPhoneNumber());
            }
        }

        // Update duration minutes if provided or can be calculated
        if (dto.getDurationMinutes() != null) {
            log.debug("Updating durationMinutes from {} to {}", existingLog.getDurationMinutes(), dto.getDurationMinutes());
            existingLog.setDurationMinutes(dto.getDurationMinutes());
        } 
        // Calculate or recalculate duration if we have start and end times
        else if (existingLog.getStartedAt() != null && existingLog.getEndedAt() != null) {
            // Recalculate even if we already have a value, as the timestamps might have been updated
            long seconds = java.time.Duration.between(existingLog.getStartedAt(), existingLog.getEndedAt()).getSeconds();
            double minutes = seconds / 60.0f;
            log.debug("Calculated durationMinutes from timestamps: {} (was: {})", minutes, existingLog.getDurationMinutes());
            existingLog.setDurationMinutes(minutes);
        }

        // Update conversation data if provided and it's more complete than what we have
        if (dto.getConversationData() != null && !dto.getConversationData().isEmpty()) {
            if (existingLog.getConversationData() == null || existingLog.getConversationData().isEmpty() ||
                dto.getConversationData().length() > existingLog.getConversationData().length()) {
                log.debug("Updating conversationData (old length: {}, new length: {})",
                        existingLog.getConversationData() != null ? existingLog.getConversationData().length() : 0,
                        dto.getConversationData().length());
                existingLog.setConversationData(dto.getConversationData());
            }
        }

        // Always update raw payload with the latest one if provided
        // This helps with debugging and seeing what the latest webhook contained
        if (dto.getRawPayload() != null && !dto.getRawPayload().isEmpty()) {
            existingLog.setRawPayload(dto.getRawPayload());
        }

        // Save the updated entity
        VoiceLog savedVoiceLog = voiceLogRepository.save(existingLog);
        return voiceLogMapper.toDto(savedVoiceLog);
    }

    /**
     * Create or update a voice log using an idempotent pattern
     * 
     * Logic:
     * 1. If a log with the same externalCallId exists:
     *    a. If the incoming webhook has required field updates (audioUrl, timestamps, etc.): Update the existing log
     *    b. Otherwise, check if it has a transcript before updating
     * 2. If no log exists yet: Create a new log using all provided values
     * 
     * This ensures we have one row per externalCallId and fields are properly updated.
     * 
     * @param dto Voice log create DTO
     * @return Created or updated voice log DTO
     */
    @Override
    @Transactional
    public VoiceLogDTO createVoiceLog(VoiceLogCreateDTO dto) {
        // Handle the case where externalCallId is null or empty
        if (dto.getExternalCallId() == null || dto.getExternalCallId().trim().isEmpty()) {
            return createNewVoiceLog(dto);
        }

        // Log incoming important fields
        log.info("Processing voice log create/update request for externalCallId: {}, audioUrl: {}, startedAt: {}, endedAt: {}, durationMinutes: {}",
                dto.getExternalCallId(),
                dto.getAudioUrl() != null ? "present" : "missing",
                dto.getStartedAt(),
                dto.getEndedAt(),
                dto.getDurationMinutes());

        // Use a synchronized block with the external call ID as the lock object to prevent race conditions
        // when multiple webhooks for the same call arrive simultaneously
        String externalCallId = dto.getExternalCallId().trim();
        synchronized (externalCallId.intern()) {  // Use intern() to ensure we use the same String object for the same call ID
            // Check again inside the synchronized block
            Optional<VoiceLog> existingLogOpt = voiceLogRepository.findByExternalCallId(externalCallId);

            if (existingLogOpt.isPresent()) {
                VoiceLog existingLog = existingLogOpt.get();
                log.info("Found existing voice log ID {} for external call ID {}", existingLog.getId(), externalCallId);

                // Determine if this update has important data we should capture regardless of transcript
                boolean hasImportantUpdates = false;

                // Check if incoming webhook has any critical field updates that should be applied
                if (dto.getAudioUrl() != null && !dto.getAudioUrl().isEmpty() && 
                    (existingLog.getAudioUrl() == null || existingLog.getAudioUrl().isEmpty())) {
                    log.info("Webhook contains audioUrl which is missing in existing record");
                    hasImportantUpdates = true;
                }

                if (dto.getDurationMinutes() != null && existingLog.getDurationMinutes() == null) {
                    log.info("Webhook contains durationMinutes which is missing in existing record");
                    hasImportantUpdates = true;
                }

                if (dto.getStartedAt() != null && existingLog.getStartedAt() == null) {
                    log.info("Webhook contains startedAt which is missing in existing record");
                    hasImportantUpdates = true;
                }

                if (dto.getEndedAt() != null && existingLog.getEndedAt() == null) {
                    log.info("Webhook contains endedAt which is missing in existing record");
                    hasImportantUpdates = true;
                }

                // Check if status should be updated (e.g., call is now completed)
                if (dto.getStatus() != null && existingLog.getStatus() != dto.getStatus() && 
                    (existingLog.getStatus() == VoiceLog.Status.INITIATED || 
                     existingLog.getStatus() == VoiceLog.Status.IN_PROGRESS || 
                     existingLog.getStatus() == VoiceLog.Status.RINGING)) {
                    log.info("Webhook contains updated status: {} (current: {})", dto.getStatus(), existingLog.getStatus());
                    hasImportantUpdates = true;
                }

                // First webhook created the log, subsequent webhooks should update if they have important data or a transcript
                // For ElevenLabs, we want to update transcript even if it's the only new data
                boolean hasTranscript = dto.getTranscript() != null && !dto.getTranscript().isEmpty();
                
                if (!hasImportantUpdates && !hasTranscript) {
                    log.info("Skipping update for external call ID {} as webhook has no important updates or transcript", externalCallId);
                    // Skip updating if no important data or transcript is provided in this webhook
                    return voiceLogMapper.toDto(existingLog);
                }
                
                // If we have a transcript, consider it an important update
                if (hasTranscript) {
                    hasImportantUpdates = true;
                    log.info("Webhook contains transcript, treating as important update");
                }

                log.info("Updating existing voice log ID {} with new data", existingLog.getId());
                // Update the existing log
                return updateVoiceLog(existingLog, dto);
            } else {
                log.info("No existing voice log found for external call ID {}, creating new record", externalCallId);
                // No existing log found, create a new one
                return createNewVoiceLog(dto);
            }
        }
    }

    /**
     * Save a voice log using the idempotent pattern
     * This method is a wrapper around createVoiceLog for backward compatibility
     * 
     * @param dto Voice log create DTO
     * @return Created or updated voice log DTO
     */
    @Override
    @Transactional
    public VoiceLogDTO save(VoiceLogCreateDTO dto) {
        // Delegate to createVoiceLog which handles the idempotent pattern
        return createVoiceLog(dto);
    }

    @Override
    @Transactional
    public VoiceLogDTO updateVoiceLogStatus(Long id, VoiceLog.Status status) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));

        voiceLog.setStatus(status);

        // If completed, set end time if not already set
        if (status == VoiceLog.Status.COMPLETED && voiceLog.getEndedAt() == null) {
            voiceLog.setEndedAt(LocalDateTime.now());
        }
        VoiceLog updatedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(updatedVoiceLog);
    }

    @Override
    @Transactional
    public void deleteVoiceLog(Long id) {
        VoiceLog voiceLog = voiceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voice log not found with id: " + id));

        voiceLogRepository.delete(voiceLog);
    }
}


