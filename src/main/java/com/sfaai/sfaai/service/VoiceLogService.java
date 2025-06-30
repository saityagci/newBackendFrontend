package com.sfaai.sfaai.service;


import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface VoiceLogService {

    /**
     * Get voice log by id
     * @param id Voice log id
     * @return Voice log DTO
     */
    VoiceLogDTO getVoiceLogById(Long id);

    /**
     * Get all voice logs
     * @return List of voice log DTOs
     */
    List<VoiceLogDTO> getAllVoiceLogs();

    /**
     * Get voice logs with pagination
     * @param pageable Pagination information
     * @return Page of voice log DTOs
     */
    Page<VoiceLogDTO> getVoiceLogs(Pageable pageable);

    /**
     * Get voice logs for a client
     * @param clientId Client id
     * @return List of voice log DTOs
     */
    List<VoiceLogDTO> getVoiceLogsByClientId(Long clientId);

    /**
     * Get voice logs for an agent
     * @param agentId Agent id
     * @return List of voice log DTOs
     */
    List<VoiceLogDTO> getVoiceLogsByAgentId(Long agentId);

    /**
     * Get voice logs for a specific date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of voice log DTOs
     */
    List<VoiceLogDTO> getVoiceLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get voice log by external call ID
     * @param externalCallId External call ID
     * @return Voice log DTO or null if not found
     */
    VoiceLogDTO getVoiceLogByExternalCallId(String externalCallId);

    /**
     * Create a new voice log or update an existing one if it has the same externalCallId
     * Important fields that should be included in the DTO:
     * - audioUrl: URL to the audio recording
     * - durationMinutes: Call duration in minutes
     * - startedAt: Call start time
     * - endedAt: Call end time
     * 
     * @param dto Voice log create DTO
     * @return Created or updated voice log DTO
     */
    VoiceLogDTO createVoiceLog(VoiceLogCreateDTO dto);

    /**
     * Alias for createVoiceLog - provides the same functionality
     * @param dto Voice log create DTO
     * @return Created or updated voice log DTO
     */
    VoiceLogDTO save(VoiceLogCreateDTO dto);
    /**
     * Update voice log status
     * @param id Voice log id
     * @param status New status
     * @return Updated voice log DTO
     */
    VoiceLogDTO updateVoiceLogStatus(Long id, VoiceLog.Status status);

    /**
     * Delete a voice log
     * @param id Voice log id
     */
    void deleteVoiceLog(Long id);
}


