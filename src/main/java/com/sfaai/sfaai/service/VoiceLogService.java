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
     * Create a new voice log
     * @param dto Voice log create DTO
     * @return Created voice log DTO
     */
    VoiceLogDTO createVoiceLog(VoiceLogCreateDTO dto);
    VoiceLogDTO save(VoiceLogCreateDTO dto);

    /**
     * Save a voice log from Vapi call log
     * @param vapiCallLog The Vapi call log DTO
     * @return Saved voice log DTO
     */
    VoiceLogDTO saveVapiCallLog(com.sfaai.sfaai.dto.VapiCallLogDTO vapiCallLog);
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


