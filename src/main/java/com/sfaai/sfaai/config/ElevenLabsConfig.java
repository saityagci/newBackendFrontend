package com.sfaai.sfaai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration properties for ElevenLabs API
 */
@Configuration
@ConfigurationProperties(prefix = "elevenlabs")
@Data
@Slf4j
public class ElevenLabsConfig {

    @Value("${elevenlabs.api.key:}")
    private String apiKey;

    @Value("${elevenlabs.api.url:https://api.elevenlabs.io}")
    private String apiUrl;

    @PostConstruct
    public void init() {
        log.info("ElevenLabsConfig initialized - API URL: {}", apiUrl);
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-elevenlabs-key-here")) {
            log.info("ElevenLabs API key is configured (length: {})", apiKey.length());
        } else {
            log.error("ElevenLabs API key is not properly configured! Current value: '{}'", apiKey);
        }
    }

    /**
     * Get the API key for ElevenLabs
     * @return The API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the API URL for ElevenLabs
     * @return The API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }
}
