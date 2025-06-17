package com.sfaai.sfaai.mapper;


import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting between Agent entities and DTOs
 */
@Component
public interface AgentMapper {

    /**
     * Convert entity to DTO
     * @param agent Agent entity
     * @return Agent DTO
     */
    AgentDTO toDto(Agent agent);

    /**
     * Convert DTO to entity
     * @param dto Agent DTO
     * @return Agent entity
     */
    Agent toEntity(AgentDTO dto);

    /**
     * Convert list of entities to list of DTOs
     * @param agents List of Agent entities
     * @return List of Agent DTOs
     */
    List<AgentDTO> toDtoList(List<Agent> agents);

    /**
     * Create entity from create DTO
     * @param dto Agent create DTO
     * @return New Agent entity
     */
    Agent createEntityFromDto(AgentCreateDTO dto);
}
