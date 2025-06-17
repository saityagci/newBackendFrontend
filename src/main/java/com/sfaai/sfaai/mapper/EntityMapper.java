package com.sfaai.sfaai.mapper;

import java.util.List;

/**
 * Generic interface for entity-DTO mapping operations
 * @param <D> DTO type
 * @param <E> Entity type
 */
public interface EntityMapper<D, E> {

    /**
     * Converts entity to DTO
     * @param entity Entity to convert
     * @return Converted DTO
     */
    D toDto(E entity);

    /**
     * Converts DTO to entity
     * @param dto DTO to convert
     * @return Converted entity
     */
    E toEntity(D dto);

    /**
     * Converts a list of entities to DTOs
     * @param entities Entities to convert
     * @return List of converted DTOs
     */
    List<D> toDtoList(List<E> entities);

    /**
     * Converts a list of DTOs to entities
     * @param dtos DTOs to convert
     * @return List of converted entities
     */
    List<E> toEntityList(List<D> dtos);
}
