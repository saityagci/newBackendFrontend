package com.sfaai.sfaai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Vapi API
 */
@Configuration
@ConfigurationProperties(prefix = "vapi")
@Data
public class VapiConfig {

    @Value("${vapi.api.key}")
    private String apiKey;

    @Value("${vapi.api.url}")
    private String apiUrl;

    /**
     * Get the API key for Vapi
     * @return The API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the API URL for Vapi
     * @return The API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }
}
