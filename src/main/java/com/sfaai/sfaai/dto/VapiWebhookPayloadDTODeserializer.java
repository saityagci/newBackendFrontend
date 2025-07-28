package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class VapiWebhookPayloadDTODeserializer extends JsonDeserializer<VapiWebhookPayloadDTO> {

    @Override
    public VapiWebhookPayloadDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);

        // Create a new DTO with an empty properties map
        VapiWebhookPayloadDTO dto = new VapiWebhookPayloadDTO();
        Map<String, Object> properties = new HashMap<>();
        dto.setProperties(properties);

        // Explicitly map duration fields to the DTO object
        if (root.has("durationMinutes")) {
            JsonNode durationNode = root.get("durationMinutes");
            if (durationNode.isNumber()) {
                dto.setDurationMinutes(durationNode.asDouble());
                log.debug("DEBUG: Found root-level durationMinutes: {}", durationNode.asDouble());
            } else if (durationNode.isTextual()) {
                try {
                    double value = Double.parseDouble(durationNode.asText());
                    dto.setDurationMinutes(value);
                    log.debug("DEBUG: Found root-level durationMinutes (string): {}", value);
                } catch (NumberFormatException e) {
                    log.debug("DEBUG: Could not parse durationMinutes: {}", durationNode.asText());
                }
            }
        }

        if (root.has("durationSeconds")) {
            JsonNode durationNode = root.get("durationSeconds");
            if (durationNode.isNumber()) {
                dto.setDurationSeconds(durationNode.asDouble());
                log.debug("DEBUG: Found root-level durationSeconds: {}", durationNode.asDouble());
            } else if (durationNode.isTextual()) {
                try {
                    double value = Double.parseDouble(durationNode.asText());
                    dto.setDurationSeconds(value);
                    log.debug("DEBUG: Found root-level durationSeconds (string): {}", value);
                } catch (NumberFormatException e) {
                    log.debug("DEBUG: Could not parse durationSeconds: {}", durationNode.asText());
                }
            }
        }

        // Log the root node for debugging
        log.debug("DEBUG: Root node: {}", root.toString());

        // 1. Handle "message" field if present
        JsonNode messageNode = root.get("message");
        if (messageNode != null && !messageNode.isNull()) {
            try {
                VapiWebhookPayloadDTO.MessageDTO message = mapper.treeToValue(messageNode, VapiWebhookPayloadDTO.MessageDTO.class);
                dto.setMessage(message);
                log.debug("DEBUG: Parsed message node: {}", message);

                // Check if there's a nested recordingUrl in artifact
                if (messageNode.has("artifact") && messageNode.get("artifact").has("recordingUrl")) {
                    String recordingUrl = messageNode.get("artifact").get("recordingUrl").asText();
                    log.debug("DEBUG: Found nested recordingUrl: {}", recordingUrl);
                }

                // Check for durationMinutes in message object
                if (messageNode.has("durationMinutes")) {
                    JsonNode durationNode = messageNode.get("durationMinutes");
                    if (durationNode.isNumber()) {
                        message.setDurationMinutes(durationNode.asDouble());
                        log.debug("DEBUG: Found message-level durationMinutes: {}", durationNode.asDouble());
                    } else if (durationNode.isTextual()) {
                        try {
                            double value = Double.parseDouble(durationNode.asText());
                            message.setDurationMinutes(value);
                            log.debug("DEBUG: Found message-level durationMinutes (string): {}", value);
                        } catch (NumberFormatException e) {
                            log.debug("DEBUG: Could not parse message durationMinutes: {}", durationNode.asText());
                        }
                    }
                }

                // Check for durationMinutes in artifact
                if (messageNode.has("artifact")) {
                    JsonNode artifactNode = messageNode.get("artifact");
                    if (artifactNode.has("durationMinutes")) {
                        JsonNode durationNode = artifactNode.get("durationMinutes");
                        if (durationNode.isNumber()) {
                            if (message.getArtifact() == null) {
                                message.setArtifact(new VapiWebhookPayloadDTO.MessageDTO.MessageArtifactDTO());
                            }
                            message.getArtifact().setDurationMinutes(durationNode.asDouble());
                            log.debug("DEBUG: Found artifact-level durationMinutes: {}", durationNode.asDouble());
                        } else if (durationNode.isTextual()) {
                            try {
                                double value = Double.parseDouble(durationNode.asText());
                                if (message.getArtifact() == null) {
                                    message.setArtifact(new VapiWebhookPayloadDTO.MessageDTO.MessageArtifactDTO());
                                }
                                message.getArtifact().setDurationMinutes(value);
                                log.debug("DEBUG: Found artifact-level durationMinutes (string): {}", value);
                            } catch (NumberFormatException e) {
                                log.debug("DEBUG: Could not parse artifact durationMinutes: {}", durationNode.asText());
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.debug("DEBUG: Error parsing message node: {}", e.getMessage());
            }

        }

        // 2. Put ALL other fields (except "message") into properties
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            // Log root-level recordingUrl if found
            if ("recordingUrl".equals(key)) {
                log.debug("DEBUG: Found root-level recordingUrl: {}", value.asText());
            }

            if (!"message".equals(key)) {
                try {
                    // Use ObjectMapper to convert JsonNode to Java Object
                    Object convertedValue = mapper.treeToValue(value, Object.class);
                    properties.put(key, convertedValue);
                } catch (Exception e) {
                    log.debug("DEBUG: Error converting field '{}': {}", key, e.getMessage());
                    // Fallback to simple conversion for this field
                    if (value.isTextual()) {
                        properties.put(key, value.asText());
                    } else if (value.isNumber()) {
                        properties.put(key, value.asDouble());
                    } else if (value.isBoolean()) {
                        properties.put(key, value.asBoolean());
                    } else if (value.isObject()) {
                        // For objects, convert to map manually
                        Map<String, Object> map = new HashMap<>();
                        value.fields().forEachRemaining(field -> {
                            try {
                                map.put(field.getKey(), mapper.treeToValue(field.getValue(), Object.class));
                            } catch (Exception ex) {
                                // If conversion fails, use toString
                                map.put(field.getKey(), field.getValue().toString());
                            }
                        });
                        properties.put(key, map);
                    }
                }
            }
        }


        return dto;


    }
}