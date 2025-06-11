package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.LoginRequest;
import com.sfaai.sfaai.dto.LoginResponse;
import com.sfaai.sfaai.dto.RegisterRequest;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
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
        System.out.println("✅ JDBC URL: " + dataSource.getConnection().getMetaData().getURL());
    }

    public LoginResponse register(RegisterRequest request) {
        Client client = Client.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER") // ✅ set default role here
                .build();

        clientRepository.save(client);

        UserDetails userDetails = userDetailsService.loadUserByUsername(client.getEmail());
        String token = jwtService.generateTokenWithRole(userDetails, client.getRole());

        return new LoginResponse(client, token);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Convert Client to UserDetails and generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(client.getEmail());
        String token = jwtService.generateTokenWithRole(userDetails, client.getRole());

        return new LoginResponse(client, token);
    }
}