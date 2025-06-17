package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final ClientRepository clientRepository;

    @Autowired
    public CustomUserDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);
        return clientRepository.findByEmail(email)
                .map(client -> {
                    // Ensure role doesn't already have ROLE_ prefix
                    String role = client.getRole().startsWith("ROLE_") 
                        ? client.getRole().substring(5) 
                        : client.getRole();
                    log.debug("User {} has role: {}", client.getEmail(), role);
                    return org.springframework.security.core.userdetails.User
                        .withUsername(client.getEmail())
                        .password(client.getPassword())
                        .roles(role)
                        .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}