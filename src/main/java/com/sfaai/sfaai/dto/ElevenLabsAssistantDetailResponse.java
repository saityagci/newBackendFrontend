package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElevenLabsAssistantDetailResponse {
    @JsonProperty("agent_id")
    private String assistantId;
    private String name;
    private String description;
    @JsonProperty("voice_id")
    private String voiceId;
    @JsonProperty("voice_name")
    private String voiceName;
    @JsonProperty("model_id")
    private String modelId;

    // Store all additional properties from the API response
    @JsonAnySetter
    private Map<String, Object> details = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getDetails() {
        return details;
    }

    // Nested config objects (add more as needed based on API)
    @JsonProperty("conversation_config")
    private ConversationConfig conversationConfig;

    @JsonProperty("agent")
    private AgentConfig agent;

    @JsonProperty("tts")
    private TtsConfig tts;

    @JsonProperty("tools")
    private Object tools; // TODO: Define a proper DTO if structure is known, else keep as Object/Map

    // Add more nested fields as needed

    // Example nested DTOs (expand as needed)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversationConfig {
        @JsonProperty("first_message")
        private String firstMessage;
        @JsonProperty("language")
        private String language;
        @JsonProperty("prompt")
        private Prompt prompt;
        @JsonProperty("agent")
        private AgentConfig agent;
        // Add more fields as needed
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Prompt {
        @JsonProperty("prompt")
        private String prompt;
        // Add more fields as needed
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentConfig {
        @JsonProperty("name")
        private String name;
        @JsonProperty("first_message")
        private String firstMessage;
        // Add more fields as needed
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsConfig {
        @JsonProperty("voice_id")
        private String voiceId;
        // Add more fields as needed
    }
} 