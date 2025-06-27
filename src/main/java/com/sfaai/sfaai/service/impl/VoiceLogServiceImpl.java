package com.sfaai.sfaai.service.impl;


import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
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

    public VoiceLogServiceImpl(VoiceLogMapper voiceLogMapper, VoiceLogRepository voiceLogRepository, VapiAssistantRepository vapiAssistantRepository) {
        this.voiceLogMapper = voiceLogMapper;
        this.voiceLogRepository = voiceLogRepository;
        this.vapiAssistantRepository = vapiAssistantRepository;
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
        // Create a new voice log entity from the DTO
        VoiceLog voiceLog = voiceLogMapper.createEntityFromDto(dto);

        // Set the VapiAssistant relationship
        if (dto.getAssistantId() != null) {
            VapiAssistant assistant = vapiAssistantRepository.findById(dto.getAssistantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assistant not found: " + dto.getAssistantId()));
            voiceLog.setVapiAssistant(assistant);
        } else if (dto.getExternalAgentId() != null) {
            // Fallback to externalAgentId if assistantId is not provided
            VapiAssistant assistant = vapiAssistantRepository.findById(dto.getExternalAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assistant not found: " + dto.getExternalAgentId()));
            voiceLog.setVapiAssistant(assistant);
        } else {
            throw new IllegalArgumentException("Assistant ID must not be null");
        }

        VoiceLog savedVoiceLog = voiceLogRepository.save(voiceLog);
        return voiceLogMapper.toDto(savedVoiceLog);
    }

    /**
     * Update an existing voice log with new data from a webhook
     * The update only proceeds if the incoming webhook contains a non-empty transcript
     * When updating, all new values are copied from the payload
     *
     * @param existingLog The existing voice log entity
     * @param dto The new data from the webhook
     * @return Updated voice log DTO
     */
    @Transactional
    protected VoiceLogDTO updateVoiceLog(VoiceLog existingLog, VoiceLogCreateDTO dto) {
        // This method will only be called if the transcript is not null and not empty
        // (that check is performed in the createVoiceLog method)
        log.debug("Updating voice log ID {} with new data from webhook. Transcript length: {}", 
                existingLog.getId(), dto.getTranscript() != null ? dto.getTranscript().length() : 0);

        // Update all fields with the new values from the DTO

        // Update status
        if (dto.getStatus() != null) {
            existingLog.setStatus(dto.getStatus());
        } else if (dto.getEndedAt() != null && existingLog.getStatus() == VoiceLog.Status.INITIATED) {
            // If status is not provided,  we have an end time, we can infer that the call is completed
            existingLog.setStatus(VoiceLog.Status.COMPLETED);
        }

        // Update timestamps if provided (never overwrite existing timestamps with null)
        if (dto.getStartedAt() != null) {
            existingLog.setStartedAt(dto.getStartedAt());
        }
        if (dto.getEndedAt() != null) {
            existingLog.setEndedAt(dto.getEndedAt());
        }

        // Update transcript only if it's not null or empty
        // If we already have a transcript, only update if the new one is better (longer)
        if (dto.getTranscript() != null && !dto.getTranscript().isEmpty()) {
            if (existingLog.getTranscript() == null || existingLog.getTranscript().isEmpty() ||
                dto.getTranscript().length() > existingLog.getTranscript().length()) {
                existingLog.setTranscript(dto.getTranscript());
            }
        }

        // Update audio URL if provided and we don't have one yet or if the new one is better
        if (dto.getAudioUrl() != null && !dto.getAudioUrl().isEmpty()) {
            if (existingLog.getAudioUrl() == null || existingLog.getAudioUrl().isEmpty() ||
                (!existingLog.getAudioUrl().equals(dto.getAudioUrl()) && 
                 // Prefer URLs with "recording" or "audio" in them as they're likely the actual recordings
                 (dto.getAudioUrl().contains("recording") || dto.getAudioUrl().contains("audio")))) {
                existingLog.setAudioUrl(dto.getAudioUrl());
            }
        }

        // Update conversation data if provided and it's more complete than what we have
        if (dto.getConversationData() != null && !dto.getConversationData().isEmpty()) {
            if (existingLog.getConversationData() == null || existingLog.getConversationData().isEmpty() ||
                dto.getConversationData().length() > existingLog.getConversationData().length()) {
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
     * Create or update a voice log using an idempotent pattern with transcript-aware updating
     * 
     * Logic:
     * 1. If a log with the same externalCallId exists:
     *    a. If the incoming webhook has a transcript: Update the existing log with all new values
     *    b. If the incoming webhook has NO transcript: Do nothing (skip the update)
     * 2. If no log exists yet: Create a new log using all provided values (even if transcript is empty)
     * 
     * This ensures we have one row per externalCallId and only update when meaningful content is available.
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

        // Use a synchronized block with the external call ID as the lock object to prevent race conditions
        // when multiple webhooks for the same call arrive simultaneously
        String externalCallId = dto.getExternalCallId().trim();
        synchronized (externalCallId.intern()) {  // Use intern() to ensure we use the same String object for the same call ID
            // Check again inside the synchronized block
            Optional<VoiceLog> existingLogOpt = voiceLogRepository.findByExternalCallId(externalCallId);

            if (existingLogOpt.isPresent()) {
                VoiceLog existingLog = existingLogOpt.get();
                log.info("Found existing voice log ID {} for external call ID {}", existingLog.getId(), externalCallId);

                // First webhook created the log, subsequent webhooks should only update if they have a transcript
                if (dto.getTranscript() == null || dto.getTranscript().isEmpty()) {
                    log.info("Skipping update for external call ID {} as the webhook has no transcript", externalCallId);
                    // Skip updating if no transcript is provided in this webhook
                    return voiceLogMapper.toDto(existingLog);
                }

                log.info("Updating existing voice log ID {} with new transcript", existingLog.getId());
                // Update the existing log only if transcript is present
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


