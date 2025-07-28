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
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        log.debug("Loading user details for email: {}", email);
        return clientRepository.findByEmail(email)
                .map(client -> {
                    // Handle null role - default to USER
                    String role = client.getRole();
                    if (role == null || role.trim().isEmpty()) {
                        role = "USER";
                    } else {
                        // Ensure role doesn't already have ROLE_ prefix
                        role = role.startsWith("ROLE_") 
                            ? role.substring(5) 
                            : role;
                    }
                    
                    log.debug("User {} has role: {}", client.getEmail(), role);
                    
                    // Handle null password
                    String password = client.getPassword() != null ? client.getPassword() : "";
                    
                    return org.springframework.security.core.userdetails.User
                        .withUsername(client.getEmail())
                        .password(password)
                        .roles(role)
                        .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}