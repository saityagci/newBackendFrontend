package com.sfaai.sfaai.service;

import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role("USER")
                .build();
    }

    @Test
    void loadUserByUsername_ExistingUser_ShouldReturnUserDetails() {
        // Arrange
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_AdminUser_ShouldReturnAdminRole() {
        // Arrange
        Client adminClient = Client.builder()
                .id(2L)
                .fullName("Admin User")
                .email("admin@example.com")
                .password("encodedPassword")
                .role("ADMIN")
                .build();
        when(clientRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminClient));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("admin@example.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        verify(clientRepository).findByEmail("admin@example.com");
    }

    @Test
    void loadUserByUsername_NonExistingUser_ShouldThrowException() {
        // Arrange
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
                customUserDetailsService.loadUserByUsername("nonexistent@example.com"));
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_NullUsername_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                customUserDetailsService.loadUserByUsername(null));
        verify(clientRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_EmptyUsername_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                customUserDetailsService.loadUserByUsername(""));
        verify(clientRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_BlankUsername_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                customUserDetailsService.loadUserByUsername("   "));
        verify(clientRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_UserWithNullRole_ShouldReturnDefaultRole() {
        // Arrange
        Client userWithNullRole = Client.builder()
                .id(3L)
                .fullName("User No Role")
                .email("norole@example.com")
                .password("encodedPassword")
                .role(null)
                .build();
        when(clientRepository.findByEmail("norole@example.com")).thenReturn(Optional.of(userWithNullRole));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("norole@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("norole@example.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        verify(clientRepository).findByEmail("norole@example.com");
    }

    @Test
    void loadUserByUsername_UserWithEmptyRole_ShouldReturnDefaultRole() {
        // Arrange
        Client userWithEmptyRole = Client.builder()
                .id(4L)
                .fullName("User Empty Role")
                .email("emptyrole@example.com")
                .password("encodedPassword")
                .role("")
                .build();
        when(clientRepository.findByEmail("emptyrole@example.com")).thenReturn(Optional.of(userWithEmptyRole));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("emptyrole@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("emptyrole@example.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
        verify(clientRepository).findByEmail("emptyrole@example.com");
    }

    @Test
    void loadUserByUsername_UserWithCustomRole_ShouldReturnCustomRole() {
        // Arrange
        Client userWithCustomRole = Client.builder()
                .id(5L)
                .fullName("Custom Role User")
                .email("custom@example.com")
                .password("encodedPassword")
                .role("MANAGER")
                .build();
        when(clientRepository.findByEmail("custom@example.com")).thenReturn(Optional.of(userWithCustomRole));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("custom@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("custom@example.com", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER")));
        verify(clientRepository).findByEmail("custom@example.com");
    }

    @Test
    void loadUserByUsername_RepositoryException_ShouldThrowException() {
        // Arrange
        when(clientRepository.findByEmail("error@example.com"))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
                customUserDetailsService.loadUserByUsername("error@example.com"));
        verify(clientRepository).findByEmail("error@example.com");
    }

    @Test
    void loadUserByUsername_UserWithNullPassword_ShouldHandleGracefully() {
        // Arrange
        Client userWithNullPassword = Client.builder()
                .id(6L)
                .fullName("User No Password")
                .email("nopassword@example.com")
                .password(null)
                .role("USER")
                .build();
        when(clientRepository.findByEmail("nopassword@example.com")).thenReturn(Optional.of(userWithNullPassword));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("nopassword@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("nopassword@example.com", result.getUsername());
        assertEquals("", result.getPassword()); // Empty string instead of null
        verify(clientRepository).findByEmail("nopassword@example.com");
    }
} 