package com.sfaai.sfaai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for audio handling
 */
@Configuration
@ConfigurationProperties(prefix = "audio")
public class AudioConfiguration {

    /**
     * Base URL for audio files, including host and port
     * This will be used when generating public URLs for audio files
     */
    private String baseUrl = "http://localhost:8880";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
