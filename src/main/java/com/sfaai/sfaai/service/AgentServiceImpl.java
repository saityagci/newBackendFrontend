package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    private final AgentRepository agentRepository;

    @Override
    public List<AgentDTO> getAllAgents() {
        List<Agent> agents = agentRepository.findAll();
        return agents.stream()
                .map(agent -> new AgentDTO(
                        agent.getId(),
                        agent.getName(),
                        agent.getType(),
                        agent.getStatus(),
                        agent.getClientId()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public AgentDTO updateAgent(Long id, AgentDTO dto) {
        return null;
    }

    @Override
    public AgentDTO createAgent(AgentDTO dto) {
        Agent agent = new Agent();
        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setClientId(dto.getClientId());
        Agent saved = agentRepository.save(agent);
        // Map back to DTO
        AgentDTO result = new AgentDTO();
        result.setId(saved.getId());
        result.setName(saved.getName());
        result.setType(saved.getType());
        result.setStatus(saved.getStatus());
        result.setClientId(saved.getClientId());
        return result;
    }

    @Override
    public AgentDTO getAgent(Long id) {
        Agent agent = agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found with id: "+id));
        return new AgentDTO(
                agent.getId(),
                agent.getName(),
                agent.getType(),
                agent.getStatus(),
                agent.getClientId()
        );
    }

    public void deleteAgent(Long id) {
        agentRepository.deleteById(id);
    }



}
