
package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.AgentMapper;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.specs.AgentSpecs;
import com.sfaai.sfaai.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;
    private final ClientRepository clientRepository;
    private final AgentMapper agentMapper;


    @Override
    @Transactional(readOnly = true)
    public AgentDTO getAgent(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        return agentMapper.toDto(agent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDTO> getAllAgents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Agent> agents = agentRepository.findAll(pageable);
        return agentMapper.toDtoList(agents.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDTO> getAllAgents() {
        List<Agent> agents = agentRepository.findAll();
        return agentMapper.toDtoList(agents);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentDTO> getAgents(Pageable pageable, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            Page<Agent> agents = agentRepository.findAll(pageable);
            return agents.map(agentMapper::toDto);
        }

        // Build specification based on filters
        Specification<Agent> spec = Specification.where(null);

        if (filters.containsKey("clientId")) {
            spec = spec.and(AgentSpecs.hasClientId((Long) filters.get("clientId")));
        }

        if (filters.containsKey("status")) {
            spec = spec.and(AgentSpecs.hasStatus((AgentStatus) filters.get("status")));
        }

        if (filters.containsKey("type")) {
            spec = spec.and(AgentSpecs.hasType((AgentType) filters.get("type")));
        }

        if (filters.containsKey("name")) {
            spec = spec.and(AgentSpecs.nameLike((String) filters.get("name")));
        }

        if (filters.containsKey("createdAfter")) {
            spec = spec.and(AgentSpecs.createdAfter((LocalDateTime) filters.get("createdAfter")));
        }

        if (filters.containsKey("createdBefore")) {
            spec = spec.and(AgentSpecs.createdBefore((LocalDateTime) filters.get("createdBefore")));
        }

        Page<Agent> agents = agentRepository.findAll(spec, pageable);
        return agents.map(agentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDTO> getAgentsByClientId(Long clientId) {
        List<Agent> agents = agentRepository.findByClientId(clientId);
        return agents.stream()
                .map(agentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgentDTO> getAgentsByClientId(Long clientId, Pageable pageable) {
        Page<Agent> agents = agentRepository.findByClientId(clientId, pageable);
        return agents.map(agentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDTO> getAgentsByType(AgentType type) {
        List<Agent> agents = agentRepository.findByType(type);
        return agents.stream()
                .map(agentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDTO> getAgentsByStatus(AgentStatus status) {
        List<Agent> agents = agentRepository.findByStatus(status);
        return agents.stream()
                .map(agentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countAgentsByClientId(Long clientId) {
        return agentRepository.countByClientId(clientId);
    }

    @Override
    @Transactional
    public AgentDTO updateAgentStatus(Long id, AgentStatus status) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));

        agent.setStatus(status);
        Agent updated = agentRepository.save(agent);
        return agentMapper.toDto(updated);
    }

    @Override
    @Transactional
    public AgentDTO createAgent(AgentCreateDTO dto) {
        // Use the mapper to convert DTO to entity
        Agent agent = agentMapper.createEntityFromDto(dto);
        Agent savedAgent = agentRepository.save(agent);
        return agentMapper.toDto(savedAgent);
    }

    @Override
    @Transactional
    public AgentDTO updateAgent(Long id, AgentDTO dto) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));

        // Update fields
        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setDescription(dto.getDescription());

        // If client is changing, verify the new client exists
        if (dto.getClientId() != null && !dto.getClientId().equals(agent.getClient().getId())) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));
            agent.setClient(client);
        }

        // Save and return
        Agent updated = agentRepository.save(agent);
        return agentMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteAgent(Long id) {
        if (!agentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agent not found with id: " + id);
        }
        agentRepository.deleteById(id);
    }
}
