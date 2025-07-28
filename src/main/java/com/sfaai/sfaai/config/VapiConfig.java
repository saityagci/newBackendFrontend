package com.sfaai.sfaai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration properties for Vapi API
 */
@Configuration
@ConfigurationProperties(prefix = "vapi")
@Data
@Slf4j
public class VapiConfig {

    @Value("${vapi.api.key}")
    private String apiKey;

    @Value("${vapi.api.url}")
    private String apiUrl;

    @PostConstruct
    public void init() {
        log.info("VapiConfig initialized - API URL: {}", apiUrl);
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-vapi-key-here")) {
            log.info("Vapi API key is configured (length: {})", apiKey.length());
        } else {
            log.error("Vapi API key is not properly configured! Current value: '{}'", apiKey);
        }
    }

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
