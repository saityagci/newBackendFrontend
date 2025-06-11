package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.service.AgentService;
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
                        agent.getClientId(),
                        agent.getDescription(),
                        agent.getCreatedAt(),
                        agent.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public AgentDTO updateAgent(Long id, AgentDTO dto) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent not found with id: " + id));

        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setClientId(dto.getClientId());
        agent.setDescription(dto.getDescription());
        // If you update timestamps or other fields, do it here

        agentRepository.save(agent);

        return new AgentDTO(
                agent.getId(),
                agent.getName(),
                agent.getType(),
                agent.getStatus(),
                agent.getClientId(),
                agent.getDescription(),
                agent.getUpdatedAt(),
                agent.getCreatedAt()
        );
    }

    @Override
    public AgentDTO createAgent(AgentDTO dto) {
        Agent agent = new Agent();
        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setClientId(dto.getClientId());
        agent.setDescription(dto.getDescription());
        agent.setCreatedAt(dto.getCreatedAt());
        agent.setUpdatedAt(dto.getUpdatedAt());
        Agent saved = agentRepository.save(agent);
        // Map back to DTO
        AgentDTO result = new AgentDTO();
        result.setId(saved.getId());
        result.setName(saved.getName());
        result.setType(saved.getType());
        result.setStatus(saved.getStatus());
        result.setClientId(saved.getClientId());
        result.setDescription(saved.getDescription());
        result.setCreatedAt(saved.getCreatedAt());
        result.setUpdatedAt(saved.getUpdatedAt());
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
                agent.getClientId(),
                agent.getDescription(),
                agent.getUpdatedAt(),
                agent.getCreatedAt()
        );
    }

    public void deleteAgent(Long id) {
        agentRepository.deleteById(id);
    }

    @Override
    public List<AgentDTO> getAgentsByClientId(Long clientId) {
        List<Agent> agents = agentRepository.findByClientId(clientId);
        return agents.stream().map(
                agent -> new AgentDTO(
                        agent.getId(),
                        agent.getName(),
                        agent.getType(),
                        agent.getStatus(),
                        agent.getClientId(),
                        agent.getDescription(),
                        agent.getCreatedAt(),
                        agent.getUpdatedAt()
                )).collect(Collectors.toList());

    }
    }
