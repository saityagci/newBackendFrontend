package com.sfaai.sfaai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiCreateAssistantRequest;
import com.sfaai.sfaai.dto.VapiCreateAssistantResponse;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVapiController.class)
class AdminVapiControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private VapiAgentService vapiAgentService;
    @MockBean
    private VapiAssistantSyncService vapiAssistantSyncService;

    private VapiAssistantDTO testAssistant;
    private VapiCreateAssistantRequest createRequest;
    private VapiCreateAssistantResponse createResponse;
    private VapiListAssistantsResponse listResponse;

    @BeforeEach
    void setUp() {
        testAssistant = VapiAssistantDTO.builder().assistantId("1").name("Test Assistant").build();
        createRequest = VapiCreateAssistantRequest.builder().firstMessage("Test Assistant").build();
        createResponse = VapiCreateAssistantResponse.builder().assistantId("1").build();
        listResponse = VapiListAssistantsResponse.builder().assistants(Arrays.asList(testAssistant)).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAssistants_ShouldReturnAssistants() throws Exception {
        when(vapiAgentService.getAllAssistants()).thenReturn(listResponse);
        mockMvc.perform(get("/api/admin/vapi/assistants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assistants[0].id").value("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAssistant_ShouldReturnCreated() throws Exception {
        when(vapiAgentService.createAssistant(any(VapiCreateAssistantRequest.class))).thenReturn(createResponse);
        mockMvc.perform(post("/api/admin/vapi/assistants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void getAllAssistants_Unauthorized_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/vapi/assistants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllAssistants_Forbidden_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/vapi/assistants"))
                .andExpect(status().isForbidden());
    }

    // Add more tests for error cases, sync, and other endpoints as needed
} 