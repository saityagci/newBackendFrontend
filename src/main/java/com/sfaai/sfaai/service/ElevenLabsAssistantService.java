package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;

import java.util.List;

/**
 * Service for ElevenLabs assistant operations
 */
public interface ElevenLabsAssistantService {

    /**
     * Get all ElevenLabs assistants from the API
     * @return List of assistants response
     */
    ElevenLabsListAssistantsResponse getAllAssistantsFromApi();

    /**
     * Get all ElevenLabs assistants from the database
     * @return List of assistant DTOs
     */
    List<ElevenLabsAssistantDTO> getAllAssistants();

    /**
     * Get a specific ElevenLabs assistant by ID
     * @param assistantId Assistant ID
     * @return Assistant DTO if found
     */
    ElevenLabsAssistantDTO getAssistant(String assistantId);

    /**
     * Synchronize all assistants from the ElevenLabs API to the database
     * @return Number of assistants synchronized
     */
    int syncAllAssistants();
}
