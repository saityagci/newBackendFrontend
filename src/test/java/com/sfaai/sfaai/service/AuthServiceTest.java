package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.LoginRequest;
import com.sfaai.sfaai.dto.LoginResponse;
import com.sfaai.sfaai.dto.RegisterRequest;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.impl.AuthService;
import com.sfaai.sfaai.service.impl.CustomUserDetailsService;
import com.sfaai.sfaai.service.impl.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Client client;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() throws SQLException {
        // Setup test data
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        client = new Client();
        client.setId(1L);
        client.setFullName("Test User");
        client.setEmail("test@example.com");
        client.setPassword("encodedPassword");
        client.setPhone("+1234567890");
        client.setRole("USER");
        client.setApiKey("test-api-key");

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("encodedPassword")
                .roles("USER")
                .build();

        // Setup DataSource mock (lenient to avoid unnecessary stubbing errors)
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.getMetaData()).thenReturn(databaseMetaData);
        lenient().when(databaseMetaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/test");
    }

    @Test
    void register_Success() {
        // Arrange
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateTokenWithRole(any(UserDetails.class), anyString())).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("/dashboard", response.getRedirectUrl());
        assertEquals("USER", response.getUser().getRole());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(clientRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(clientRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email is already registered", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void register_InvalidEmail() {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Invalid email format", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void register_MissingPassword() {
        // Arrange
        registerRequest.setPassword(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    void login_Success() {
        // Arrange
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateTokenWithRole(any(UserDetails.class), anyString())).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("/dashboard", response.getRedirectUrl());
        assertEquals("USER", response.getUser().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateTokenWithRole(any(UserDetails.class), eq("USER"));
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_AuthenticationFailed() {
        // Arrange
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_AdminUser() {
        // Arrange
        client.setRole("ADMIN");
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.of(client));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.generateTokenWithRole(any(UserDetails.class), anyString())).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertEquals("/AdminDashboard", response.getRedirectUrl());
        assertEquals("ADMIN", response.getUser().getRole());
    }

    @Test
    void login_MissingEmail() {
        // Arrange
        loginRequest.setEmail(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email and password are required", exception.getMessage());
    }

    @Test
    void login_MissingPassword() {
        // Arrange
        loginRequest.setPassword(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email and password are required", exception.getMessage());
    }
} 