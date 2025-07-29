package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.ClientDTO;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.service.ClientService;
import com.sfaai.sfaai.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private ObjectMapper objectMapper;

    private Client testClient;
    private ClientDTO testClientDTO;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFullName("Test User");
        testClient.setEmail("test@example.com");
        testClient.setPhone("+1234567890");
        testClient.setRole("USER");

        testClientDTO = ClientDTO.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@example.com")
                .phone("+1234567890")
                .role("USER")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "ADMIN")
    void getAllClients_ShouldReturnClients() throws Exception {
        // Arrange
        List<ClientDTO> clients = Arrays.asList(testClientDTO);
        when(clientService.findAll()).thenReturn(clients);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Test User"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"));

        verify(clientService).findAll(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getClientById_WithValidId_ShouldReturnClient() throws Exception {
        // Arrange
        when(clientService.findById(1L)).thenReturn(testClientDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(clientService).findById(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getClientById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(clientService.findById(999L)).thenThrow(new RuntimeException("Client not found"));

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isInternalServerError());

        verify(clientService).findById(999L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "ADMIN")
    void deleteClient_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(clientService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(clientService).delete(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getCurrentUser_WithValidUser_ShouldReturnUser() throws Exception {
        // Arrange
        when(clientService.findByEmail("test@example.com")).thenReturn(testClientDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(clientService).findByEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getCurrentUser_WithInvalidUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(clientService.findByEmail("test@example.com")).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());

        verify(clientService).findByEmail("test@example.com");
    }

    @Test
    void getAllClients_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        verify(clientService, never()).findAll(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getAllClients_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(clientService, never()).findAll(any());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteClient_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());

        verify(clientService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateClient_WithValidData_ShouldReturnUpdatedClient() throws Exception {
        // Arrange
        ClientDTO updateDTO = ClientDTO.builder()
                .id(1L)
                .fullName("Updated User")
                .email("updated@example.com")
                .phone("+9876543210")
                .role("USER")
                .build();

        when(clientService.update(eq(1L), any(ClientDTO.class))).thenReturn(updateDTO);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.phone").value("+9876543210"));

        verify(clientService).update(eq(1L), any(ClientDTO.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateClient_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(clientService.update(eq(999L), any(ClientDTO.class)))
                .thenThrow(new RuntimeException("Client not found"));

        // Act & Assert
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testClientDTO)))
                .andExpect(status().isNotFound());

        verify(clientService).update(eq(999L), any(ClientDTO.class));
    }

    @Test
    void updateClient_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testClientDTO)))
                .andExpect(status().isUnauthorized());

        verify(clientService, never()).update(anyLong(), any(ClientDTO.class));
    }

    @Test
    void getCurrentUser_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        verify(clientService, never()).findByEmail(anyString());
    }
} 