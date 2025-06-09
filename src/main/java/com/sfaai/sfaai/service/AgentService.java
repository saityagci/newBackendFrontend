package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AgentDTO;

import java.util.List;

public interface AgentService {
    AgentDTO createAgent(AgentDTO dto);
    AgentDTO getAgent(Long id);
    List<AgentDTO> getAllAgents();
    AgentDTO updateAgent(Long id, AgentDTO dto);
    void deleteAgent(Long id);
}
