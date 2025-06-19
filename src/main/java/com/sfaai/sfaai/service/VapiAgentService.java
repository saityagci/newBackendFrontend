package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VapiCreateAssistantRequest;
import com.sfaai.sfaai.dto.VapiCreateAssistantResponse;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;

/**
 * Service for Vapi agent operations
 */
public interface VapiAgentService {

    /**
     * Create a new Vapi assistant
     * @param request Assistant creation data
     * @return Created assistant response
     */
    VapiCreateAssistantResponse createAssistant(VapiCreateAssistantRequest request);

    /**
     * Get all Vapi assistants
     * @return List of assistants response
     */
    VapiListAssistantsResponse getAllAssistants();
}
