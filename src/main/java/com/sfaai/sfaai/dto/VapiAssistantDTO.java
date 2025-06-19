package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Vapi assistant data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiAssistantDTO {

    @JsonProperty("id")
    private String assistantId;

    private String name;
    private String status;

    // Voice information
    private VoiceInfo voice;

    // Model information
    private ModelInfo model;

    // Transcriber information
    private TranscriberInfo transcriber;

    // First message
    private String firstMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VoiceInfo {
        private String provider;
        private String voiceId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelInfo {
        private String provider;
        private String model;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TranscriberInfo {
        private String provider;
        private String model;
        private String language;
    }
}
