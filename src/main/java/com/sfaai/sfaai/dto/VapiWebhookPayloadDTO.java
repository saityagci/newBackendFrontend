package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@JsonDeserialize(using = VapiWebhookPayloadDTODeserializer.class)
public class VapiWebhookPayloadDTO {

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    // @JsonAnySetter removed as it's now handled by our custom deserializer
    public void add(String key, Object value) {
        properties.put(key, value);
    }
    // At class level
    @JsonProperty("durationMinutes")
    private Double durationMinutes;

    @JsonProperty("durationSeconds")
    private Double durationSeconds;

// ... more if you want

    // (optional, if you want to use @JsonAnySetter)
    @JsonAnySetter
    public void set(String key, Object value) {
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
        private Double durationMinutes;

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
            private Double durationMinutes;
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
     * Get any available recording URL from the webhook payload.
     * Vapi sometimes sends the recording URL at different locations including:
     * - At the root level as "recordingUrl"
     * - Nested inside message.artifact.recordingUrl
     * - Inside a call object as call.recordingUrl
     * - Inside an artifact object as artifact.recordingUrl or artifact.recording_url
     * - Inside a recording object as recording.url
     *
     * This method checks all common locations to ensure we don't miss any format.
     *
     * @return The first available recording URL from any of the common locations
     */
    public String getAnyRecordingUrl() {
        // Log all properties for debugging
        System.out.println("DEBUG getAnyRecordingUrl - Properties: " + properties);
        if (message != null && message.getArtifact() != null) {
            System.out.println("DEBUG getAnyRecordingUrl - Message artifact: " + message.getArtifact());
            System.out.println("DEBUG getAnyRecordingUrl - Message artifact recordingUrl: " +
                    message.getArtifact().getRecordingUrl());
        }

        // First check direct root property - highest priority
        if (properties.containsKey("recordingUrl")) {
            Object url = properties.get("recordingUrl");
            if (url != null && !url.toString().isEmpty()) {
                System.out.println("DEBUG getAnyRecordingUrl - Found root recordingUrl: " + url);
                return url.toString();
            }
        }

        // Then try the nested path via getAudioUrl()
        String nestedUrl = getAudioUrl();
        if (nestedUrl != null && !nestedUrl.isEmpty()) {
            System.out.println("DEBUG getAnyRecordingUrl - Found nested audioUrl: " + nestedUrl);
            return nestedUrl;
        }

        // Check common paths for recording URLs
        String[] possiblePaths = {
            // Direct root property with alternative names
            "recording_url",
            "audioUrl",
            "audio_url",
            // Inside call object
            "call.recordingUrl",
            "call.recording_url",
            // Inside artifact object
            "artifact.recordingUrl",
            "artifact.recording_url",
            "artifact.audioUrl",
            "artifact.audio_url",
            // Inside recording object
            "recording.url"
        };

        // Check all defined paths
        for (String path : possiblePaths) {
            String url = getStringValue(path);
            if (url != null && !url.isEmpty()) {
                System.out.println("DEBUG getAnyRecordingUrl - Found URL at path '" + path + "': " + url);
                return url;
            }
        }

        // Direct access to properties as a final fallback
        for (String key : properties.keySet()) {
            if ((key.toLowerCase().contains("recording") || key.toLowerCase().contains("audio")) &&
                key.toLowerCase().contains("url")) {
                Object value = properties.get(key);
                if (value != null) {
                    System.out.println("DEBUG getAnyRecordingUrl - Found URL in property '" + key + "': " + value);
                    return value.toString();
                }
            }

            // Check nested maps (try one level deep)
            if (properties.get(key) instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) properties.get(key);
                for (String nestedKey : nestedMap.keySet()) {
                    if ((nestedKey.toLowerCase().contains("recording") ||
                         nestedKey.toLowerCase().contains("audio")) &&
                        nestedKey.toLowerCase().contains("url")) {
                        Object value = nestedMap.get(nestedKey);
                        if (value != null) {
                            System.out.println("DEBUG getAnyRecordingUrl - Found URL in nested property '" +
                                    key + "." + nestedKey + "': " + value);
                            return value.toString();
                        }
                    }
                }
            }
        }

        System.out.println("DEBUG getAnyRecordingUrl - No recording URL found");
        return null;
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
    /**
     * Extract the total conversation duration in minutes from the webhook payload
     * with multiple fallback options for different duration formats.
     *
     * @return Duration in minutes as a Double, rounded to 4 decimal places, or null if not available
     */
    public Double extractDurationMinutes() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VapiWebhookPayloadDTO.class);
        log.debug("Extracting duration in minutes from payload (minutes only, no conversion)");

        // For debugging only - log all key paths that might contain duration
        logAllPotentialDurationPaths();

        // 0. Check POJO field
        if (this.durationMinutes != null) {
            log.debug("Found durationMinutes at root POJO field: {}", this.durationMinutes);
            return roundToFourDecimals(this.durationMinutes);
        }

        // 0.1 Check if we have durationSeconds and convert
        if (this.durationSeconds != null) {
            Double minutes = this.durationSeconds / 60.0;
            log.debug("Converting durationSeconds to minutes: {} seconds = {} minutes", this.durationSeconds, minutes);
            return roundToFourDecimals(minutes);
        }


        // 1. Check message object structure first - this should be properly populated by the deserializer
        if (message != null) {
            // 1.1 Direct durationMinutes in the message object
            if (message.getDurationMinutes() != null) {
                log.debug("Found durationMinutes in message object: {}", message.getDurationMinutes());
                return roundToFourDecimals(message.getDurationMinutes());
            }

            // 1.2 Check for durationMinutes in the artifact
            if (message.getArtifact() != null && message.getArtifact().getDurationMinutes() != null) {
                log.debug("Found durationMinutes in message.artifact: {}", message.getArtifact().getDurationMinutes());
                return roundToFourDecimals(message.getArtifact().getDurationMinutes());
            }
        }

        // 2. Fall back to checking for "durationMinutes" in message map (should be redundant with above)
        if (message != null) {
            Map<String, Object> messageMap = getMapValue("message");
            if (messageMap != null && messageMap.containsKey("durationMinutes")) {
                Object durationObj = messageMap.get("durationMinutes");
                Double durationMinutes = parseNumberToDouble(durationObj);
                if (durationMinutes != null) {
                    log.debug("Found durationMinutes in message map: {}", durationMinutes);
                    return roundToFourDecimals(durationMinutes);
                }
            }

            // 2.1 Check artifact map as well
            Map<String, Object> artifactMap = getMapValue("message.artifact");
            if (artifactMap != null && artifactMap.containsKey("durationMinutes")) {
                Object durationObj = artifactMap.get("durationMinutes");
                Double durationMinutes = parseNumberToDouble(durationObj);
                if (durationMinutes != null) {
                    log.debug("Found durationMinutes in message.artifact map: {}", durationMinutes);
                    return roundToFourDecimals(durationMinutes);
                }
            }
        }

        // 3. Check for "durationMinutes" at root level
        Object durationObj = properties.get("durationMinutes");
        Double durationMinutes = parseNumberToDouble(durationObj);
        if (durationMinutes != null) {
            log.debug("Found durationMinutes at root level: {}", durationMinutes);
            return roundToFourDecimals(durationMinutes);
        }

        // 4. Try nested fields with the name "durationMinutes"
        String[] minutePaths = {
                "durationMinutes",
                "call.durationMinutes",
                "artifact.durationMinutes",
                "recording.durationMinutes"
        };
        for (String path : minutePaths) {
            Object value = getNestedValue(path);
            Double duration = parseNumberToDouble(value);
            if (duration != null) {
                log.debug("Found durationMinutes at {}: {}", path, duration);
                return roundToFourDecimals(duration);
            }
        }

        // 5. Calculate from timestamps using various field names and formats
        // Common field name pairs for start/end times
        String[][] timeFieldPairs = {
            // Standard ISO timestamp fields
            {"call.createdAt", "call.updatedAt"},
            {"call.created_at", "call.updated_at"},
            {"createdAt", "updatedAt"},
            {"created_at", "updated_at"},

            // Start/end time pairs
            {"call.startTime", "call.endTime"},
            {"call.start_time", "call.end_time"},
            {"startTime", "endTime"},
            {"start_time", "end_time"},
            {"startedAt", "endedAt"},
            {"started_at", "ended_at"},

            // Timestamps in artifact
            {"artifact.startTime", "artifact.endTime"},
            {"artifact.start_time", "artifact.end_time"},
            {"message.artifact.startTime", "message.artifact.endTime"},
            {"message.artifact.start_time", "message.artifact.end_time"}
        };

        for (String[] fieldPair : timeFieldPairs) {
            String startField = fieldPair[0];
            String endField = fieldPair[1];

            String startTimeStr = getStringValue(startField);
            String endTimeStr = getStringValue(endField);

            if (startTimeStr != null && endTimeStr != null) {
                try {
                    // Try multiple date formats
                    java.time.Instant start = parseTimestamp(startTimeStr);
                    java.time.Instant end = parseTimestamp(endTimeStr);

                    if (start != null && end != null) {
                        long diffMs = java.time.Duration.between(start, end).toMillis();
                        double callMinutes = diffMs / 60000.0;
                        if (callMinutes > 0.001) {
                            log.debug("Calculated durationMinutes from {} and {}: {}", startField, endField, callMinutes);
                            return roundToFourDecimals(callMinutes);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to parse timestamps from {} and {}: {}", startField, endField, e.getMessage());
                    // Continue trying other formats
                }
            }
        }

        log.debug("No durationMinutes found in payload");
        return null;
    }

    // Helper methods:
    private Double parseNumberToDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try {
                return Double.parseDouble(((String) obj).trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Double roundToFourDecimals(Double value) {
        if (value == null) return null;
        return Math.round(value * 10000.0) / 10000.0;
    }

    /**
     * Parse a timestamp string in multiple formats:
     * - ISO-8601 format (2023-07-01T12:34:56Z)
     * - Unix timestamp in seconds (1625144096)
     * - Unix timestamp in milliseconds (1625144096000)
     * 
     * @param timestampStr The timestamp string to parse
     * @return An Instant representing the timestamp, or null if parsing fails
     */
    private java.time.Instant parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            return null;
        }

        // Try ISO format first
        try {
            return java.time.Instant.parse(timestampStr);
        } catch (Exception e) {
            // Not ISO format, try other formats
        }

        // Try unix timestamp (seconds)
        try {
            long timestamp = Long.parseLong(timestampStr);
            // If timestamp is too small to be milliseconds, assume it's seconds
            if (timestamp < 31536000000L) { // ~1 year in milliseconds
                return java.time.Instant.ofEpochSecond(timestamp);
            } else {
                return java.time.Instant.ofEpochMilli(timestamp);
            }
        } catch (NumberFormatException e) {
            // Not a number
        }

        // Try other date formats
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestampStr, formatter);
                return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
            } catch (Exception e) {
                // Try next format
            }
        }

        return null; // All parsing attempts failed
    }

    /**
     * Debug helper method to log all potential paths that might contain duration information
     */
    private void logAllPotentialDurationPaths() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VapiWebhookPayloadDTO.class);

        // Log root level values
        log.debug("Root-level durationMinutes field: {}", this.durationMinutes);
        log.debug("Root-level durationSeconds field: {}", this.durationSeconds);
        log.debug("Properties map contains durationMinutes: {}", properties.containsKey("durationMinutes"));
        log.debug("Properties map durationMinutes value: {}", properties.get("durationMinutes"));

        // Log nested values in message
        if (this.message != null) {
            log.debug("Message.durationMinutes: {}", this.message.getDurationMinutes());

            if (this.message.getArtifact() != null) {
                log.debug("Message.artifact.durationMinutes: {}", this.message.getArtifact().getDurationMinutes());
            } else {
                log.debug("Message.artifact is null");
            }

            // Log nested call timestamps if present
            if (this.message.getCall() != null) {
                log.debug("Message has call data but no timestamp information available");
            } else {
                log.debug("Message.call is null");
            }
        } else {
            log.debug("Message object is null");
        }

        // Log nested values via getNestedValue
        String[] durationPaths = {
            "durationMinutes", "duration_minutes", "duration", 
            "call.durationMinutes", "call.duration_minutes", "call.duration",
            "artifact.durationMinutes", "artifact.duration_minutes", "artifact.duration",
            "message.durationMinutes", "message.duration_minutes", "message.duration",
            "message.artifact.durationMinutes", "message.artifact.duration_minutes", "message.artifact.duration"
        };

        for (String path : durationPaths) {
            Object value = getNestedValue(path);
            if (value != null) {
                log.debug("Found value at path '{}': {}", path, value);
            }
        }
    }


    /**
     * Get duration in minutes from the payload (legacy method, use extractDurationMinutes instead)
     * @deprecated Use extractDurationMinutes() for more robust duration extraction
     */
    @Deprecated
    public Double getDurationMinutes() {
        Object val = properties.get("durationMinutes");
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) {
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
