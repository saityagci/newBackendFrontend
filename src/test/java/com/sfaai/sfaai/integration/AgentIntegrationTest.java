package com.sfaai.sfaai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {com.sfaai.sfaai.config.TestConfig.class})
@Transactional
class AgentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient;
    private AgentCreateDTO testAgentCreateDTO;

    @BeforeEach
    void setUp() {
        // Create a test client
        testClient = new Client();
        testClient.setFullName("Test Client");
        testClient.setEmail("test@client.com");
        testClient.setRole("USER");
        testClient.setPassword("password123");
        testClient.setApiKey("test-api-key-123");
        testClient = clientRepository.save(testClient);

        // Create test agent DTO
        testAgentCreateDTO = AgentCreateDTO.builder()
                .name("Test Agent")
                .type(Agent.AgentType.VAPI)
                .status(Agent.AgentStatus.ACTIVE)
                .description("Test agent description")
                .clientId(testClient.getId())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAgent_Integration_ShouldCreateAndReturnAgent() throws Exception {
        // Act
        String response = mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Agent"))
                .andExpect(jsonPath("$.type").value("VAPI"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientId").value(testClient.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        AgentDTO createdAgent = objectMapper.readValue(response, AgentDTO.class);
        assertThat(createdAgent.getId()).isNotNull();
        assertThat(createdAgent.getName()).isEqualTo("Test Agent");

        // Verify in database
        Agent savedAgent = agentRepository.findById(createdAgent.getId()).orElse(null);
        assertThat(savedAgent).isNotNull();
        assertThat(savedAgent.getName()).isEqualTo("Test Agent");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAgentById_Integration_ShouldReturnAgent() throws Exception {
        // Arrange
        Agent savedAgent = agentRepository.save(createTestAgent());

        // Act & Assert
        mockMvc.perform(get("/api/agents/" + savedAgent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAgent.getId()))
                .andExpect(jsonPath("$.name").value("Test Agent"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAgents_Integration_ShouldReturnAllAgents() throws Exception {
        // Arrange
        agentRepository.save(createTestAgent());
        agentRepository.save(createTestAgent());

        // Act & Assert
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAgentsByClient_Integration_ShouldReturnClientAgents() throws Exception {
        // Arrange
        agentRepository.save(createTestAgent());
        agentRepository.save(createTestAgent());

        // Act & Assert
        mockMvc.perform(get("/api/agents/client/" + testClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAgent_Integration_ShouldUpdateAndReturnAgent() throws Exception {
        // Arrange
        Agent savedAgent = agentRepository.save(createTestAgent());
        AgentDTO updateDTO = AgentDTO.builder()
                .name("Updated Agent")
                .type(Agent.AgentType.N8N)
                .status(Agent.AgentStatus.INACTIVE)
                .description("Updated description")
                .clientId(testClient.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/agents/" + savedAgent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Agent"))
                .andExpect(jsonPath("$.type").value("N8N"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAgent_Integration_ShouldDeleteAgent() throws Exception {
        // Arrange
        Agent savedAgent = agentRepository.save(createTestAgent());

        // Act & Assert
        mockMvc.perform(delete("/api/agents/" + savedAgent.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        assertThat(agentRepository.findById(savedAgent.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void agentLifecycle_Integration_ShouldWorkEndToEnd() throws Exception {
        // Create
        String createResponse = mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AgentDTO createdAgent = objectMapper.readValue(createResponse, AgentDTO.class);

        // Read
        mockMvc.perform(get("/api/agents/" + createdAgent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Agent"));

        // Update
        AgentDTO updateDTO = AgentDTO.builder()
                .name("Updated Agent")
                .type(Agent.AgentType.N8N)
                .status(Agent.AgentStatus.INACTIVE)
                .description("Updated description")
                .clientId(testClient.getId())
                .build();

        mockMvc.perform(put("/api/agents/" + createdAgent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Agent"));

        // Delete
        mockMvc.perform(delete("/api/agents/" + createdAgent.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        assertThat(agentRepository.findById(createdAgent.getId())).isEmpty();
    }

    @Test
    void createAgent_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAgent_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAgentCreateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAgent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AgentCreateDTO invalidDTO = AgentCreateDTO.builder()
                .name("") // Invalid: empty name
                .type(Agent.AgentType.VAPI)
                .clientId(testClient.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAgentById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/agents/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAgent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        AgentDTO updateDTO = AgentDTO.builder()
                .name("Updated Agent")
                .type(Agent.AgentType.N8N)
                .status(Agent.AgentStatus.INACTIVE)
                .description("Updated description")
                .clientId(testClient.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/agents/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAgent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/agents/999"))
                .andExpect(status().isNotFound());
    }

    private Agent createTestAgent() {
        Agent agent = new Agent();
        agent.setName("Test Agent");
        agent.setType(Agent.AgentType.VAPI);
        agent.setStatus(Agent.AgentStatus.ACTIVE);
        agent.setDescription("Test agent description");
        agent.setClient(testClient);
        return agent;
    }
} 