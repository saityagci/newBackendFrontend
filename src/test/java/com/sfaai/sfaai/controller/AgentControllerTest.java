package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.service.AgentService;
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

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AgentCreateDTO testAgentCreateDTO;
    private AgentDTO testAgentDTO;

    @BeforeEach
    void setUp() {
        testAgentCreateDTO = AgentCreateDTO.builder()
                .name("Test Agent")
                .description("Test Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .clientId(1L)
                .build();

        testAgentDTO = AgentDTO.builder()
                .id(1L)
                .name("Test Agent")
                .description("Test Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .clientId(1L)
                .build();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createAgent_WithValidData_ShouldReturnCreatedAgent() throws Exception {
        // Arrange
        when(agentService.createAgent(any(AgentCreateDTO.class))).thenReturn(testAgentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Agent"))
                .andExpect(jsonPath("$.type").value("VAPI"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(agentService).createAgent(any(AgentCreateDTO.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createAgent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AgentCreateDTO invalidDTO = AgentCreateDTO.builder()
                .name("") // Invalid: empty name
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .clientId(1L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(agentService, never()).createAgent(any(AgentCreateDTO.class));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void createAgent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isForbidden());

        verify(agentService, never()).createAgent(any(AgentCreateDTO.class));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAgentById_WithValidId_ShouldReturnAgent() throws Exception {
        // Arrange
        when(agentService.getAgent(1L)).thenReturn(testAgentDTO);

        // Act & Assert
        mockMvc.perform(get("/api/agents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Agent"))
                .andExpect(jsonPath("$.type").value("VAPI"));

        verify(agentService).getAgent(1L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAgentById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(agentService.getAgent(999L)).thenThrow(new ResourceNotFoundException("Agent not found"));

        // Act & Assert
        mockMvc.perform(get("/api/agents/999"))
                .andExpect(status().isNotFound());

        verify(agentService).getAgent(999L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAllAgents_ShouldReturnAgents() throws Exception {
        // Arrange
        List<AgentDTO> agents = Arrays.asList(testAgentDTO);
        when(agentService.getAllAgents(0, 20)).thenReturn(agents);

        // Act & Assert
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Agent"));

        verify(agentService).getAllAgents(0, 20);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAllAgents_WithCustomPagination_ShouldReturnAgents() throws Exception {
        // Arrange
        List<AgentDTO> agents = Arrays.asList(testAgentDTO);
        when(agentService.getAllAgents(1, 10)).thenReturn(agents);

        // Act & Assert
        mockMvc.perform(get("/api/agents?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(agentService).getAllAgents(1, 10);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateAgent_WithValidData_ShouldReturnUpdatedAgent() throws Exception {
        // Arrange
        AgentDTO updatedDTO = AgentDTO.builder()
                .id(1L)
                .name("Updated Agent")
                .description("Updated Description")
                .type(AgentType.N8N)
                .status(AgentStatus.INACTIVE)
                .clientId(1L)
                .build();

        when(agentService.updateAgent(eq(1L), any(AgentDTO.class))).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/agents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Agent"))
                .andExpect(jsonPath("$.type").value("N8N"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(agentService).updateAgent(eq(1L), any(AgentDTO.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateAgent_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(agentService.updateAgent(eq(999L), any(AgentDTO.class)))
                .thenThrow(new ResourceNotFoundException("Agent not found"));

        // Act & Assert
        mockMvc.perform(put("/api/agents/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentDTO)))
                .andExpect(status().isNotFound());

        verify(agentService).updateAgent(eq(999L), any(AgentDTO.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteAgent_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(agentService).deleteAgent(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/agents/1"))
                .andExpect(status().isNoContent());

        verify(agentService).deleteAgent(1L);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteAgent_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Agent not found")).when(agentService).deleteAgent(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/agents/999"))
                .andExpect(status().isNotFound());

        verify(agentService).deleteAgent(999L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAgentsByClient_WithValidClientId_ShouldReturnAgents() throws Exception {
        // Arrange
        List<AgentDTO> agents = Arrays.asList(testAgentDTO);
        when(agentService.getAgentsByClientId(1L)).thenReturn(agents);

        // Act & Assert
        mockMvc.perform(get("/api/agents/by-client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Agent"));

        verify(agentService).getAgentsByClientId(1L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAgentsByClient_WithInvalidClientId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(agentService.getAgentsByClientId(999L))
                .thenThrow(new ResourceNotFoundException("Client not found"));

        // Act & Assert
        mockMvc.perform(get("/api/agents/by-client/999"))
                .andExpect(status().isNotFound());

        verify(agentService).getAgentsByClientId(999L);
    }

    @Test
    void createAgent_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isUnauthorized());

        verify(agentService, never()).createAgent(any(AgentCreateDTO.class));
    }

    @Test
    void getAgentById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/agents/1"))
                .andExpect(status().isUnauthorized());

        verify(agentService, never()).getAgent(anyLong());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void updateAgent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/agents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentDTO)))
                .andExpect(status().isForbidden());

        verify(agentService, never()).updateAgent(anyLong(), any(AgentDTO.class));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void deleteAgent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/agents/1"))
                .andExpect(status().isForbidden());

        verify(agentService, never()).deleteAgent(anyLong());
    }
} 