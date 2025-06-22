package com.sfaai.sfaai.integration;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class VapiAssistantSyncIntegrationTest {

    @Autowired
    private VapiAssistantSyncService vapiAssistantSyncService;

    @Autowired
    private VapiAssistantRepository vapiAssistantRepository;

    @MockBean
    private VapiAgentService vapiAgentService;

    @Test
    void synchronizeAllAssistants_IntegrationTest() {
        // Arrange
        // Create test DTOs
        VapiAssistantDTO dto1 = new VapiAssistantDTO();
        dto1.setAssistantId("int-test-1");
        dto1.setName("Integration Test 1");
        dto1.setStatus("active");

        VapiAssistantDTO dto2 = new VapiAssistantDTO();
        dto2.setAssistantId("int-test-2");
        dto2.setName("Integration Test 2");
        dto2.setStatus("active");

        // Setup mock API response
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(dto1, dto2));
        when(vapiAgentService.getAllAssistants()).thenReturn(response);

        // Act
        int count = vapiAssistantSyncService.synchronizeAllAssistants();

        // Assert
        assertEquals(2, count);

        // Verify database state
        List<VapiAssistant> assistants = vapiAssistantRepository.findAll();
        assertEquals(2, assistants.size());

        Optional<VapiAssistant> assistant1 = vapiAssistantRepository.findById("int-test-1");
        assertTrue(assistant1.isPresent());
        assertEquals("Integration Test 1", assistant1.get().getName());
        assertEquals("active", assistant1.get().getStatus());
        assertNotNull(assistant1.get().getLastSyncedAt());
        assertEquals("SUCCESS", assistant1.get().getSyncStatus());

        Optional<VapiAssistant> assistant2 = vapiAssistantRepository.findById("int-test-2");
        assertTrue(assistant2.isPresent());
        assertEquals("Integration Test 2", assistant2.get().getName());
    }

    @Test
    void synchronizeAssistant_IntegrationTest() {
        // Arrange
        String testId = "int-test-single";

        // Create test DTO
        VapiAssistantDTO dto = new VapiAssistantDTO();
        dto.setAssistantId(testId);
        dto.setName("Single Integration Test");
        dto.setStatus("active");

        // Setup mock API response
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(dto));
        when(vapiAgentService.getAllAssistants()).thenReturn(response);

        // Act
        boolean result = vapiAssistantSyncService.synchronizeAssistant(testId);

        // Assert
        assertTrue(result);

        // Verify database state
        Optional<VapiAssistant> assistant = vapiAssistantRepository.findById(testId);
        assertTrue(assistant.isPresent());
        assertEquals("Single Integration Test", assistant.get().getName());
        assertEquals("active", assistant.get().getStatus());
    }
}
