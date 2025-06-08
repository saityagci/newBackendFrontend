package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.ClientDTO;

import java.util.List;

public interface ClientService {
    ClientDTO save(ClientDTO dto);
    List<ClientDTO> findAll();
    ClientDTO findById(Long id);
    ClientDTO findByEmail(String email);
    void delete(Long id);
    ClientDTO getClientByApiKey(String apiKey);
}
