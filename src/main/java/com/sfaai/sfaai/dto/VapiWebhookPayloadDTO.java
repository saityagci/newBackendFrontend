package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@ToString
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
