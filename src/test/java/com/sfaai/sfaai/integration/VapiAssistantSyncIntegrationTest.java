package com.sfaai.sfaai.integration;

import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {com.sfaai.sfaai.config.TestConfig.class})
@Transactional
class VapiAssistantSyncIntegrationTest {

    @Autowired
    private VapiAssistantSyncService vapiAssistantSyncService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client testClient;

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
    }

    @Test
    void synchronizeAssistant_IntegrationTest() {
        // This test would normally test the actual synchronization
        // For now, we'll just verify the service is available
        assertThat(vapiAssistantSyncService).isNotNull();
        
        // Verify we can access the repositories
        assertThat(agentRepository).isNotNull();
        assertThat(clientRepository).isNotNull();
        
        // Verify test client was created
        assertThat(testClient.getId()).isNotNull();
    }

    @Test
    void synchronizeAllAssistants_IntegrationTest() {
        // This test would normally test the actual synchronization
        // For now, we'll just verify the service is available
        assertThat(vapiAssistantSyncService).isNotNull();
        
        // Verify we can access the repositories
        assertThat(agentRepository).isNotNull();
        assertThat(clientRepository).isNotNull();
        
        // Verify test client was created
        assertThat(testClient.getId()).isNotNull();
    }
}
