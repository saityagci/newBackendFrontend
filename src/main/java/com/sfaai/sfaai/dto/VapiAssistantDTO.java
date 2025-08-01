package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * DTO for Vapi assistant data
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class VapiAssistantDTO {

    @JsonProperty("id")
    private String assistantId;

    // Remove the conflicting getId() method - Jackson will use the field directly
    // The @JsonProperty("id") annotation on assistantId field handles the mapping

    private String name;
    private String status;
    private String description;

    // Add client and agent relationship fields
    @JsonProperty("client_id")
    private Long clientId;

    @JsonProperty("assigned_agent_id")
    private Long agentId;

    // Try multiple ways to map the first_message field
    @JsonProperty(value = "firstMessage")
    private String firstMessage;

    @JsonProperty("first_message")
    public void setFirstMessageFromJson(String message) {
        this.firstMessage = message;
        log.debug("Setting firstMessage via first_message annotation: {}", message);
    }

    // Try alternate field names that API might be using
    @JsonProperty("initial_message")
    public void setInitialMessage(String message) {
        if (this.firstMessage == null) {
            this.firstMessage = message;
            log.debug("Setting firstMessage via initial_message annotation: {}", message);
        }
    }

    @JsonProperty("default_message")
    public void setDefaultMessage(String message) {
        if (this.firstMessage == null) {
            this.firstMessage = message;
            log.debug("Setting firstMessage via default_message annotation: {}", message);
        }
    }

    @JsonProperty("greeting")
    public void setGreeting(String message) {
        if (this.firstMessage == null) {
            this.firstMessage = message;
            log.debug("Setting firstMessage via greeting annotation: {}", message);
        }
    }

    @JsonProperty("welcome_message")
    public void setWelcomeMessage(String message) {
        if (this.firstMessage == null) {
            this.firstMessage = message;
            log.debug("Setting firstMessage via welcome_message annotation: {}", message);
        }
    }

   public void setFirstMessage(String firstMessage) {
       this.firstMessage = firstMessage;
       log.debug("Setting firstMessage via direct setter: {}", firstMessage);
       // Print stack trace to see which code path is calling this method
       if (firstMessage == null) {
           log.warn("WARNING: Null value being set for firstMessage");
           Thread.dumpStack();
       }
   }

    public String getFirstMessage() {
        return firstMessage;
    }

    private VoiceInfo voice;
    private ModelInfo model;
    private TranscriberInfo transcriber;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VoiceInfo {
        private String provider;

        @JsonProperty("voice_id")
        private String voiceId;

        @Override
        public String toString() {
            return "VoiceInfo(provider=" + provider + ", voiceId=" + voiceId + ")";
        }
    }

  @Setter
  @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModelInfo {
        private String provider;
        private String model;
    }

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TranscriberInfo {
        private String provider;
        private String model;
        private String language;
        @Override
        public String toString() {
            return "TranscriberInfo{" +
                    "language='" + language + '\'' +
                    ", provider='" + provider + '\'' +
                    ", model='" + model + '\'' +
                    '}';
        }
    }
}