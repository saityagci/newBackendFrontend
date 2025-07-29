package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.ClientCreateDTO;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.ClientMapper;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client testClient;
    private ClientDTO testClientDTO;
    private ClientCreateDTO testClientCreateDTO;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .role("USER")
                .password("encodedPassword")
                .apiKey("test-api-key")
                .build();

        testClientDTO = ClientDTO.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .role("USER")
                .build();

        testClientCreateDTO = ClientCreateDTO.builder()
                .fullName("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .password("password123")
                .role("USER")
                .build();
    }

    @Test
    void save_ValidClient_ShouldReturnCreatedClient() {
        // Arrange
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

        // Act
        ClientDTO result = clientService.save(testClientCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testClientDTO.getId(), result.getId());
        assertEquals(testClientDTO.getEmail(), result.getEmail());
        verify(clientRepository).existsByEmail(testClientCreateDTO.getEmail());
        verify(passwordEncoder).encode(testClientCreateDTO.getPassword());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void save_DuplicateEmail_ShouldThrowException() {
        // Arrange
        when(clientRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clientService.save(testClientCreateDTO));
        verify(clientRepository).existsByEmail(testClientCreateDTO.getEmail());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void findById_ExistingClient_ShouldReturnClient() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientMapper.toDto(testClient)).thenReturn(testClientDTO);

        // Act
        ClientDTO result = clientService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testClientDTO.getId(), result.getId());
        verify(clientRepository).findById(1L);
    }

    @Test
    void findById_NonExistingClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(999L));
        verify(clientRepository).findById(999L);
    }

    @Test
    void findByEmail_ExistingClient_ShouldReturnClient() {
        // Arrange
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testClient));
        when(clientMapper.toDto(testClient)).thenReturn(testClientDTO);

        // Act
        ClientDTO result = clientService.findByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testClientDTO.getEmail(), result.getEmail());
        verify(clientRepository).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NonExistingClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.findByEmail("nonexistent@example.com"));
        verify(clientRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void findAll_ShouldReturnAllClients() {
        // Arrange
        List<Client> clients = Arrays.asList(testClient);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

        // Act
        List<ClientDTO> result = clientService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertEquals(testClientDTO.getId(), result.get(0).getId());
        verify(clientRepository).findAll();
        verify(clientMapper).toDto(testClient);
    }

    @Test
    void findAll_WithPagination_ShouldReturnPaginatedClients() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Client> clientPage = new PageImpl<>(Arrays.asList(testClient));
        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(clientMapper.toDto(any(Client.class))).thenReturn(testClientDTO);

        // Act
        Page<ClientDTO> result = clientService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testClientDTO.getId(), result.getContent().get(0).getId());
        verify(clientRepository).findAll(pageable);
    }

    @Test
    void update_ExistingClient_ShouldReturnUpdatedClient() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(clientMapper.toDto(testClient)).thenReturn(testClientDTO);

        // Act
        ClientDTO result = clientService.update(1L, testClientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testClientDTO.getId(), result.getId());
        verify(clientRepository).findById(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void update_NonExistingClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.update(999L, testClientDTO));
        verify(clientRepository).findById(999L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void delete_ExistingClient_ShouldDeleteSuccessfully() {
        // Arrange
        when(clientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clientRepository).deleteById(1L);

        // Act
        clientService.delete(1L);

        // Assert
        verify(clientRepository).existsById(1L);
        verify(clientRepository).deleteById(1L);
    }

    @Test
    void delete_NonExistingClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.delete(999L));
        verify(clientRepository).existsById(999L);
        verify(clientRepository, never()).deleteById(anyLong());
    }

    @Test
    void getClientByApiKey_ExistingClient_ShouldReturnClient() {
        // Arrange
        when(clientRepository.findByApiKey("test-api-key")).thenReturn(Optional.of(testClient));
        when(clientMapper.toDto(testClient)).thenReturn(testClientDTO);

        // Act
        ClientDTO result = clientService.getClientByApiKey("test-api-key");

        // Assert
        assertNotNull(result);
        assertEquals(testClientDTO.getId(), result.getId());
        verify(clientRepository).findByApiKey("test-api-key");
    }

    @Test
    void getClientByApiKey_NonExistingClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.findByApiKey("invalid-key")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.getClientByApiKey("invalid-key"));
        verify(clientRepository).findByApiKey("invalid-key");
    }
} 