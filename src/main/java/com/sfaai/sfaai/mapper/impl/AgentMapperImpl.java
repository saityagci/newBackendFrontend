package com.sfaai.sfaai.mapper.impl;

import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.AgentMapper;
import com.sfaai.sfaai.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgentMapperImpl implements AgentMapper {

    private final ClientRepository clientRepository;

    @Override
    public AgentDTO toDto(Agent agent) {
        if (agent == null) {
            return null;
        }

        return AgentDTO.builder()
                .id(agent.getId())
                .name(agent.getName())
                .type(agent.getType())
                .status(agent.getStatus())
                .description(agent.getDescription())
                .clientId(agent.getClient() != null ? agent.getClient().getId() : null)
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }

    @Override
    public Agent toEntity(AgentDTO dto) {
        if (dto == null) {
            return null;
        }

        Agent agent = new Agent();
        agent.setId(dto.getId());
        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setDescription(dto.getDescription());

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));
            agent.setClient(client);
        }

        return agent;
    }

    @Override
    public List<AgentDTO> toDtoList(List<Agent> agents) {
        return agents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Agent createEntityFromDto(AgentCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Agent agent = new Agent();
        agent.setName(dto.getName());
        agent.setType(dto.getType());
        agent.setStatus(dto.getStatus());
        agent.setDescription(dto.getDescription());

        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + dto.getClientId()));
            agent.setClient(client);
        }

        return agent;
    }
}
