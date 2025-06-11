package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;


    private ClientDTO toDto(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .role(client.getRole())
                .build();
    }

    private Client toEntity(ClientDTO dto) {
        return Client.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : "USER")
                .build();
    }

    public ClientDTO save(ClientDTO dto) {
        Client saved = clientRepository.save(toEntity(dto));
        return toDto(saved);
    }

    public List<ClientDTO> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ClientDTO findById(Long id) {
        return clientRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }
    public ClientDTO findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public ClientDTO getClientByApiKey(String apiKey) {
        Client client = clientRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return toDto(client);
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}
