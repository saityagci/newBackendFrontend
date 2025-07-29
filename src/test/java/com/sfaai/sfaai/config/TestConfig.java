package com.sfaai.sfaai.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import com.sfaai.sfaai.service.VapiAgentService;
import com.sfaai.sfaai.service.impl.VapiAgentServiceImpl;
import com.sfaai.sfaai.service.ElevenLabsAssistantService;
import com.sfaai.sfaai.service.VapiAssistantSyncService;
import com.sfaai.sfaai.service.AudioStorageService;
import com.sfaai.sfaai.util.WebhookSignatureVerifier;
import com.sfaai.sfaai.util.FirstMessageFallbackAdapter;

@TestConfiguration
public class TestConfig {

    // Mock external services to prevent real API calls during testing
    @MockBean
    private VapiAgentService vapiAgentService;

    @MockBean
    private ElevenLabsAssistantService elevenLabsAssistantService;

    @MockBean
    private VapiAssistantSyncService vapiAssistantSyncService;

    @MockBean
    private AudioStorageService audioStorageService;

    @MockBean
    private WebhookSignatureVerifier webhookSignatureVerifier;

    @MockBean
    private FirstMessageFallbackAdapter firstMessageFallbackAdapter;

    // Mock ElevenLabsConfig to prevent environment variable resolution issues
    @Bean
    @Primary
    public ElevenLabsConfig elevenLabsConfig() {
        ElevenLabsConfig config = new ElevenLabsConfig();
        config.setApiKey("test-elevenlabs-key-mock");
        config.setApiUrl("https://api.elevenlabs.io");
        return config;
    }

    // Mock VapiConfig to prevent environment variable resolution issues
    @Bean
    @Primary
    public VapiConfig vapiConfig() {
        VapiConfig config = new VapiConfig();
        config.setApiKey("test-vapi-key-mock");
        config.setApiUrl("https://api.vapi.ai");
        return config;
    }

    // Provide a test RestTemplate
    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
        return new RestTemplate();
    }

    // Provide the actual VapiAgentServiceImpl bean for tests that need it
    @Bean
    @Primary
    public VapiAgentServiceImpl vapiAgentServiceImpl(VapiConfig vapiConfig, RestTemplate restTemplate, FirstMessageFallbackAdapter firstMessageFallbackAdapter) {
        return new VapiAgentServiceImpl(restTemplate, vapiConfig, firstMessageFallbackAdapter);
    }
} 