package com.sfaai.sfaai.service;

/**
 * Service for security and authorization checks
 */
public interface SecurityService {

    /**
     * Checks if the current authenticated user has access to the specified client
     * @param clientId The client ID to check access for
     * @return true if the user has access, false otherwise
     */
    boolean hasClientAccess(Long clientId);

    /**
     * Checks if the current authenticated user has access to the specified agent
     * @param agentId The agent ID to check access for
     * @return true if the user has access, false otherwise
     */
    boolean hasAgentAccess(Long agentId);

    /**
     * Checks if the current authenticated user has access to the specified voice log
     * @param voiceLogId The voice log ID to check access for
     * @return true if the user has access, false otherwise
     */
    boolean hasVoiceLogAccess(Long voiceLogId);

    /**
     * Checks if the current authenticated user has access to the specified workflow log
     * @param workflowLogId The workflow log ID to check access for
     * @return true if the user has access, false otherwise
     */
    boolean hasWorkflowLogAccess(Long workflowLogId);
}
