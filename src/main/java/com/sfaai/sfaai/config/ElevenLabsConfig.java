package com.sfaai.sfaai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for ElevenLabs API
 */
@Configuration
@ConfigurationProperties(prefix = "elevenlabs")
@Data
public class ElevenLabsConfig {

    @Value("${elevenlabs.api.key}")
    private String apiKey;

    @Value("${elevenlabs.api.url:https://api.elevenlabs.io}")
    private String apiUrl;

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
