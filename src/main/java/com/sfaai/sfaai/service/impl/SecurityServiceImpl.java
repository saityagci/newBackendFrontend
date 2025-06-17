package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.repository.WorkflowLogRepository;
import com.sfaai.sfaai.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final ClientRepository clientRepository;
    private final AgentRepository agentRepository;
    private final VoiceLogRepository voiceLogRepository;
    private final WorkflowLogRepository workflowLogRepository;

    @Override
    public boolean hasClientAccess(Long clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Log authorities for debugging
        System.out.println("Checking access for user: " + auth.getName());
        auth.getAuthorities().forEach(authority -> 
            System.out.println("Authority: " + authority.getAuthority()));

        // Admin has access to everything
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            System.out.println("Admin access granted");
            return true;
        }

        // User can only access their own data
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            // Check if the authenticated user's email matches the client's email
            boolean hasAccess = auth.getName().equals(client.get().getEmail());
            System.out.println("User access check: " + hasAccess + " for " + auth.getName() + " vs " + client.get().getEmail());
            return hasAccess;
        }

        System.out.println("Access denied: Client not found or user not authorized");
        return false;
    }

    @Override
    public boolean hasAgentAccess(Long agentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Admin has access to everything
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // For all other users, check if they own the agent
        Optional<Agent> agent = agentRepository.findById(agentId);
        if (agent.isPresent() && agent.get().getClient() != null) {
            return auth.getName().equals(agent.get().getClient().getEmail());
        }

        return false;
    }

    @Override
    public boolean hasVoiceLogAccess(Long voiceLogId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Admin has access to everything
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // For all other users, check if they own the voice log
        Optional<VoiceLog> voiceLog = voiceLogRepository.findById(voiceLogId);
        if (voiceLog.isPresent() && voiceLog.get().getClient() != null) {
            return auth.getName().equals(voiceLog.get().getClient().getEmail());
        }

        return false;
    }

    @Override
    public boolean hasWorkflowLogAccess(Long workflowLogId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Admin has access to everything
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // For all other users, check if they own the workflow log
        Optional<WorkflowLog> workflowLog = workflowLogRepository.findById(workflowLogId);
        if (workflowLog.isPresent() && workflowLog.get().getClient() != null) {
            return auth.getName().equals(workflowLog.get().getClient().getEmail());
        }

        return false;
    }
}
