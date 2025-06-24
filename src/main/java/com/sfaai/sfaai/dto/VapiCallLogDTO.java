package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidated DTO for Vapi call logs, webhook payloads, and database persistence
 * This class combines functionality from VapiWebhookPayloadDTO, VoiceLogWebhookDTO, and VoiceLogCreateDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiCallLogDTO {
    // Fields for webhook payload parsing and flexible schema support
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    // Core call identification fields
    private String callId;                // External call ID from Vapi
    private String assistantId;           // Vapi assistant ID

    // Database entity fields (matching VoiceLogCreateDTO/VoiceLogDTO)
    private Long id;                      // Database ID after persistence

    @NotNull(message = "Agent ID is required")
    private Long agentId;                 // Internal agent ID

    @NotNull(message = "Client ID is required")
    private Long clientId;                // Internal client ID

    @NotBlank(message = "Provider is required")
    @Builder.Default
    private String provider = "vapi";      // Voice provider name

    @JsonProperty("externalCallId")  // Allow mapping from multiple property names
    private String externalCallId;        // Same as callId, but for compatibility

    @JsonProperty("externalAgentId") // Allow mapping from multiple property names
    private String externalAgentId;       // Same as assistantId, but for compatibility

    // Timing fields
    private LocalDateTime startTime;      // Call start time
    private LocalDateTime endTime;        // Call end time
    private LocalDateTime startedAt;      // Same as startTime (for compatibility)
    private LocalDateTime endedAt;        // Same as endTime (for compatibility)
    private LocalDateTime createdAt;      // Database record creation time

    // Status and content fields
    private String status;                // Call status from Vapi
    private String audioUrl;              // URL to the call recording
    private String transcript;            // Full call transcript
    private String transcriptText;        // Same as transcript (for compatibility)
    private String rawPayload;            // Raw JSON payload for debugging
    private String conversationData;      // Structured conversation data (JSON)

    // Messages collection
    @Builder.Default
    private List<MessageDTO> messages = new ArrayList<>();

    /**
     * DTO for representing individual messages in the call
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class MessageDTO {
        private String role;               // "assistant" or "user"
        private String content;            // Message content
        private LocalDateTime timestamp;   // When the message was sent
    }

    /**
     * Helper method to get the correct transcript value
     * This handles compatibility between transcriptText and transcript fields
     */
    public String getTranscript() {
        return transcript != null ? transcript : transcriptText;
    }

    /**
     * Helper method to set the transcript value
     * Sets both transcript and transcriptText for compatibility
     */
    public void setTranscript(String text) {
        this.transcript = text;
        this.transcriptText = text;
    }

    /**
     * Helper method to get the call start time
     * This handles compatibility between startTime and startedAt fields
     */
    public LocalDateTime getStartedAt() {
        return startedAt != null ? startedAt : startTime;
    }

    /**
     * Helper method to set the call start time
     * Sets both startTime and startedAt for compatibility
     */
    public void setStartedAt(LocalDateTime time) {
        this.startedAt = time;
        this.startTime = time;
    }

    /**
     * Helper method to get the call end time
     * This handles compatibility between endTime and endedAt fields
     */
    public LocalDateTime getEndedAt() {
        return endedAt != null ? endedAt : endTime;
    }

    /**
     * Helper method to set the call end time
     * Sets both endTime and endedAt for compatibility
     */
    public void setEndedAt(LocalDateTime time) {
        this.endedAt = time;
        this.endTime = time;
    }

    /**
     * Helper method to get external call ID
     * This handles compatibility between callId and externalCallId fields
     */
    public String getExternalCallId() {
        return externalCallId != null ? externalCallId : callId;
    }

    /**
     * Helper method to set external call ID
     * Sets both callId and externalCallId for compatibility
     */
    public void setExternalCallId(String id) {
        this.externalCallId = id;
        this.callId = id;
    }

    /**
     * Helper method to get external agent ID
     * This handles compatibility between assistantId and externalAgentId fields
     */
    public String getExternalAgentId() {
        return externalAgentId != null ? externalAgentId : assistantId;
    }

    /**
     * Helper method to set external agent ID
     * Sets both assistantId and externalAgentId for compatibility
     */
    public void setExternalAgentId(String id) {
        this.externalAgentId = id;
        this.assistantId = id;
    }

    // Methods for handling dynamic webhook payload properties
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Helper method to get nested value using dot notation
     * @param path Path using dot notation (e.g., "call.id")
     * @return The value at the path or null if not found
     */
    @SuppressWarnings("unchecked")
    public Object getNestedValue(String path) {
        if (properties == null || path == null) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = properties;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Helper method to get a string value from the payload
     * @param path Path using dot notation
     * @return String value or null
     */
    public String getStringValue(String path) {
        Object value = getNestedValue(path);
        return value != null ? value.toString() : null;
    }

    /**
     * Helper method to get a nested map from the payload
     * @param path Path using dot notation
     * @return Map or null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMapValue(String path) {
        Object value = getNestedValue(path);
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Helper method to get a list from the payload
     * @param path Path using dot notation
     * @return List or null
     */
    @SuppressWarnings("unchecked")
    public List<Object> getListValue(String path) {
        Object value = getNestedValue(path);
        return value instanceof List ? (List<Object>) value : null;
    }
}
