package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for receiving webhook payloads from Vapi
 * Flexibly accepts any schema using a map to store all properties
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "properties") // Exclude properties from toString to avoid large log outputs
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiWebhookPayloadDTO {

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

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
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = properties;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
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

    /**
     * Helper method to get a number value from the payload
     * @param path Path using dot notation
     * @return Number value or null
     */
    public Number getNumberValue(String path) {
        Object value = getNestedValue(path);
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Helper method to get a boolean value from the payload
     * @param path Path using dot notation
     * @return Boolean value or null
     */
    public Boolean getBooleanValue(String path) {
        Object value = getNestedValue(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String strValue = ((String) value).toLowerCase();
            if (strValue.equals("true") || strValue.equals("yes") || strValue.equals("1")) {
                return true;
            } else if (strValue.equals("false") || strValue.equals("no") || strValue.equals("0")) {
                return false;
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return null;
    }

    /**
     * DTO representing the message structure from Vapi webhook
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageDTO {
        private MessageCallDTO call;
        private MessageAssistantDTO assistant;
        private MessageAnalysisDTO analysis;
        private MessageArtifactDTO artifact;
        private Long timestamp;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageCallDTO {
            private String id;
            private MessageCustomerDTO customer;

            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class MessageCustomerDTO {
                private String number;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageAssistantDTO {
            private String id;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageAnalysisDTO {
            private String summary;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageArtifactDTO {
            private String transcript;
            private String recordingUrl;
        }
    }

    @JsonProperty("message")
    private MessageDTO message;

    /**
     * Get the call ID from the message structure
     * @return The call ID or null if not available
     */
    public String getCallId() {
        return message != null && message.getCall() != null ? 
               message.getCall().getId() : null;
    }

    /**
     * Get the assistant ID from the message structure
     * @return The assistant ID or null if not available
     */
    public String getAssistantId() {
        return message != null && message.getAssistant() != null ? 
               message.getAssistant().getId() : null;
    }

    /**
     * Get the caller phone number from the message structure
     * @return The caller phone number or null if not available
     */
    public String getCallerPhoneNumber() {
        return message != null && message.getCall() != null && 
               message.getCall().getCustomer() != null ? 
               message.getCall().getCustomer().getNumber() : null;
    }

    /**
     * Get the summary from the message analysis
     * @return The summary or null if not available
     */
    public String getSummary() {
        return message != null && message.getAnalysis() != null ? 
               message.getAnalysis().getSummary() : null;
    }

    /**
     * Get the transcript from the message artifact
     * @return The transcript or null if not available
     */
    public String getTranscript() {
        return message != null && message.getArtifact() != null ? 
               message.getArtifact().getTranscript() : null;
    }

    /**
     * Get the audio URL from the message artifact
     * @return The audio URL or null if not available
     */
    public String getAudioUrl() {
        return message != null && message.getArtifact() != null ? 
               message.getArtifact().getRecordingUrl() : null;
    }

    /**
     * Get the timestamp from the message
     * @return The timestamp or null if not available
     */
    public Long getTimestamp() {
        return message != null ? message.getTimestamp() : null;
    }

    /**
     * Create a new DTO using the nested map under the specified key
     * This allows path-based lookups to work from the nested level
     * @param key The key of the nested map (e.g., "message")
     * @return A new DTO with the nested map as its properties, or this DTO if the key doesn't exist
     */
    public VapiWebhookPayloadDTO getNestedPayload(String key) {
        Map<String, Object> nestedMap = getMapValue(key);
        if (nestedMap == null) {
            return this;
        }

        VapiWebhookPayloadDTO nestedDTO = new VapiWebhookPayloadDTO();
        nestedDTO.properties = nestedMap;
        return nestedDTO;
    }
}
