package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.entity.Client;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Client entities and DTOs
 */
@Component
public class ClientMapper implements EntityMapper<ClientDTO, Client> {

    @Override
    public ClientDTO toDto(Client entity) {
        if (entity == null) {
            return null;
        }

        return ClientDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .apiKey(entity.getApiKey())
                .vapiAssistantId(entity.getVapiAssistantId())
                .vapiAssistantIds(entity.getVapiAssistantIds())
                .build();
    }

    @Override
    public Client toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }

        Client client = Client.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : "ROLE_CLIENT")
                .build();

        if (dto.getId() != null) {
            client.setId(dto.getId());
        }

        return client;
    }

    /**
     * Converts ClientCreateDTO to Client entity
     * @param dto DTO to convert
     * @return Converted entity
     */
    public Client createDtoToEntity(ClientCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        return Client.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : "ROLE_CLIENT")
                .build();
    }

    @Override
    public List<ClientDTO> toDtoList(List<Client> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> toEntityList(List<ClientDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
