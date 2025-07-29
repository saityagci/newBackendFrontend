package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;
import com.sfaai.sfaai.service.ElevenLabsAssistantService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminElevenLabsController.class)
class AdminElevenLabsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ElevenLabsAssistantService elevenLabsAssistantService;

    private ElevenLabsAssistantDTO testAssistant;
    private ElevenLabsListAssistantsResponse testListResponse;

    @BeforeEach
    void setUp() {
        testAssistant = ElevenLabsAssistantDTO.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .description("Test Description")
                .build();

        testListResponse = ElevenLabsListAssistantsResponse.builder()
                .assistants(Arrays.asList(testAssistant))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistantsFromApi_ShouldReturnAssistants() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAllAssistantsFromApi()).thenReturn(testListResponse);

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistants[0].assistantId").value("test-assistant-id"))
                .andExpect(jsonPath("$.assistants[0].name").value("Test Assistant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistantsFromApi_EmptyResponse_ShouldReturnEmptyList() throws Exception {
        // Arrange
        ElevenLabsListAssistantsResponse emptyResponse = ElevenLabsListAssistantsResponse.builder()
                .assistants(Arrays.asList())
                .build();
        when(elevenLabsAssistantService.getAllAssistantsFromApi()).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistants").isArray())
                .andExpect(jsonPath("$.assistants").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistants_ShouldReturnAssistantsFromDatabase() throws Exception {
        // Arrange
        List<ElevenLabsAssistantDTO> assistants = Arrays.asList(testAssistant);
        when(elevenLabsAssistantService.getAllAssistants()).thenReturn(assistants);

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assistantId").value("test-assistant-id"))
                .andExpect(jsonPath("$[0].name").value("Test Assistant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistants_EmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAllAssistants()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssistantById_ExistingAssistant_ShouldReturnAssistant() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAssistant("test-assistant-id")).thenReturn(testAssistant);

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/test-assistant-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistantId").value("test-assistant-id"))
                .andExpect(jsonPath("$.name").value("Test Assistant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssistantById_NonExistingAssistant_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAssistant("non-existing-id")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/non-existing-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAssistantsFromApi_Unauthorized_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/api"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllAssistantsFromApi_Forbidden_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/api"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllAssistants_Unauthorized_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllAssistants_Forbidden_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAssistantById_Unauthorized_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/test-assistant-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAssistantById_Forbidden_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/test-assistant-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistantsFromApi_ServiceException_ShouldReturn500() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAllAssistantsFromApi())
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants/api"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistants_ServiceException_ShouldReturn500() throws Exception {
        // Arrange
        when(elevenLabsAssistantService.getAllAssistants())
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/admin/elevenlabs/assistants"))
                .andExpect(status().isInternalServerError());
    }
} 