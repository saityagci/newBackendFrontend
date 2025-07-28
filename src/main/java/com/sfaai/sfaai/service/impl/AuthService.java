package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.dto.LoginRequest;
import com.sfaai.sfaai.dto.LoginResponse;
import com.sfaai.sfaai.dto.RegisterRequest;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void logDatasource() throws SQLException {
        log.info("✅ JDBC URL: {}", dataSource.getConnection().getMetaData().getURL());
    }

    public LoginResponse register(RegisterRequest request) {
        // Debug logging
        log.debug("RegisterRequest received - fullName: '{}', email: '{}'", request.getFullName(), request.getEmail());
        
        // Generate a unique API key for the new client
        String apiKey = generateApiKey();

        // Validate the request
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if email already exists
        if (clientRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Make sure role is stored without ROLE_ prefix, as Spring Security will add it
        // Use role from request or default to USER
        String role = (request.getRole() != null && !request.getRole().trim().isEmpty()) 
                ? request.getRole().trim().toUpperCase() 
                : "USER";

        Client client = Client.builder()
                .fullName(request.getFullName() != null ? request.getFullName().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(role) // Role without ROLE_ prefix
                .apiKey(apiKey) // Add the generated API key
                .build();

        // Debug logging
        log.debug("DEBUG: Client entity created - fullName: '{}', email: '{}'", client.getFullName(), client.getEmail());

        Client savedClient = clientRepository.save(client);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedClient.getEmail());
        String token = jwtService.generateTokenWithRole(userDetails, savedClient.getRole());

        // Convert to DTO to avoid lazy loading issues
        ClientDTO clientDTO = ClientDTO.builder()
            .id(savedClient.getId())
            .fullName(savedClient.getFullName())
            .email(savedClient.getEmail())
            .phone(savedClient.getPhone())
            .role(savedClient.getRole())
            .apiKey(savedClient.getApiKey())
            .build();

        return new LoginResponse(clientDTO, token);
    }

    public LoginResponse login(LoginRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().isEmpty() ||
            request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        try {
            // Normalize email address (trim and convert to lowercase)
            String normalizedEmail = request.getEmail().trim().toLowerCase();

            // Check if the user exists before attempting authentication
            Optional<Client> clientOptional = clientRepository.findByEmail(normalizedEmail);
            if (clientOptional.isEmpty()) {
                // Additional check is redundant since findByEmail should be case-insensitive
                // or we should be using findByEmailIgnoreCase consistently
                throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
            }

            Client client = clientOptional.get();

            // Authenticate with Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(client.getEmail(), request.getPassword())
            );

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(client.getEmail());
            String token = jwtService.generateTokenWithRole(userDetails, client.getRole());

            // Convert to DTO to avoid lazy loading issues
            ClientDTO clientDTO = ClientDTO.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .role(client.getRole())
                .apiKey(client.getApiKey())
                .build();

            // Log the user's role for debugging
            log.info("✅ User logged in with role: {}", client.getRole());
            log.info("✅ User logged in with email: {}", client.getEmail());

            // Create response with correct redirect URL
            String redirectUrl = client.getRole().equalsIgnoreCase("ADMIN") ? "/AdminDashboard" : "/dashboard";
            LoginResponse response = new LoginResponse(clientDTO, token, redirectUrl);

            // Log the complete response
            log.info("✅ Login Response - User Role: {}", response.getUser().getRole());
            log.info("✅ Login Response - Redirect URL: {}", response.getRedirectUrl());

            return response;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Generate a random API key for clients
     * @return A random 32-character API key
     */
    private String generateApiKey() {
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if email is valid
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}