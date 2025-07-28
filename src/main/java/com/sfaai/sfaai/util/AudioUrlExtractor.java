package com.sfaai.sfaai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting audio URLs from various data sources.
 * 
 * This class consolidates different extraction strategies into a single utility
 * to ensure consistent audio URL extraction across the application.
 */
public class AudioUrlExtractor {

    private static final Logger log = LoggerFactory.getLogger(AudioUrlExtractor.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Extract an audio URL from a JSON string
     * @param json The JSON string to search
     * @return The first audio URL found, or null if none found
     */
    public static String extractFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        log.debug("Attempting to extract audio URL from JSON string of length: {}", json.length());

        // First try JSON parsing approach
        try {
            JsonNode rootNode = mapper.readTree(json);

            // Check root level fields first
            String url = checkNode(rootNode, "recordingUrl");
            if (url != null) {
                log.debug("Found audio URL in root.recordingUrl: {}", url);
                return url;
            }

            url = checkNode(rootNode, "recording_url");
            if (url != null) {
                log.debug("Found audio URL in root.recording_url: {}", url);
                return url;
            }

            url = checkNode(rootNode, "audioUrl");
            if (url != null) {
                log.debug("Found audio URL in root.audioUrl: {}", url);
                return url;
            }

            url = checkNode(rootNode, "audio_url");
            if (url != null) {
                log.debug("Found audio URL in root.audio_url: {}", url);
                return url;
            }

            url = checkNode(rootNode, "mediaUrl");
            if (url != null) {
                log.debug("Found audio URL in root.mediaUrl: {}", url);
                return url;
            }

            url = checkNode(rootNode, "media_url");
            if (url != null) {
                log.debug("Found audio URL in root.media_url: {}", url);
                return url;
            }

            // Check message.artifact path
            if (rootNode.has("message") && rootNode.get("message").has("artifact")) {
                JsonNode artifact = rootNode.get("message").get("artifact");
                url = checkNode(artifact, "recordingUrl");
                if (url != null) {
                    log.debug("Found audio URL in message.artifact.recordingUrl: {}", url);
                    return url;
                }

                url = checkNode(artifact, "recording_url");
                if (url != null) {
                    log.debug("Found audio URL in message.artifact.recording_url: {}", url);
                    return url;
                }
            }

            // Check call object
            if (rootNode.has("call")) {
                JsonNode call = rootNode.get("call");
                url = checkNode(call, "recordingUrl");
                if (url != null) {
                    log.debug("Found audio URL in call.recordingUrl: {}", url);
                    return url;
                }

                url = checkNode(call, "recording_url");
                if (url != null) {
                    log.debug("Found audio URL in call.recording_url: {}", url);
                    return url;
                }
            }

            // Check artifact at root level
            if (rootNode.has("artifact")) {
                JsonNode artifact = rootNode.get("artifact");
                url = checkNode(artifact, "recordingUrl");
                if (url != null) {
                    log.debug("Found audio URL in artifact.recordingUrl: {}", url);
                    return url;
                }

                url = checkNode(artifact, "recording_url");
                if (url != null) {
                    log.debug("Found audio URL in artifact.recording_url: {}", url);
                    return url;
                }
            }

        } catch (JsonProcessingException e) {
            // If JSON parsing fails, fallback to regex approach
            log.warn("Failed to parse JSON, falling back to regex approach: {}", e.getMessage());
        }

        // Fallback to regex approach
        return extractWithRegex(json);
    }

    /**
     * Extract audio URL from a Map structure.
     * 
     * @param data The Map to extract URL from
     * @return The extracted URL or null if none found
     */
    public static String extractFromMap(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        log.debug("Attempting to extract audio URL from Map with keys: {}", data.keySet());

        // Check common field names directly
        String[] audioFields = {
            "recordingUrl", "recording_url", "audioUrl", "audio_url", 
            "mediaUrl", "media_url", "audio", "recording"
        };

        for (String field : audioFields) {
            Object value = data.get(field);
            if (value != null) {
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.startsWith("http") && !strValue.isEmpty()) {
                        log.debug("Found audio URL in field '{}': {}", field, strValue);
                        return strValue;
                    }
                } else if (value instanceof Map) {
                    // If the value is a nested map, look for URL field in it
                    Map<String, Object> nestedMap = (Map<String, Object>) value;
                    String[] urlFields = {"url", "link", "href"};
                    for (String urlField : urlFields) {
                        Object urlValue = nestedMap.get(urlField);
                        if (urlValue instanceof String) {
                            String strUrl = (String) urlValue;
                            if (strUrl.startsWith("http") && !strUrl.isEmpty()) {
                                log.debug("Found audio URL in nested map {}.'{}': {}", field, urlField, strUrl);
                                return strUrl;
                            }
                        }
                    }
                }
            }
        }

        // Check for recordings array
        Object recordings = data.get("recordings");
        if (recordings instanceof List && !((List<?>) recordings).isEmpty()) {
            Object firstRecording = ((List<?>) recordings).get(0);
            if (firstRecording instanceof String) {
                String url = (String) firstRecording;
                if (url.startsWith("http") && !url.isEmpty()) {
                    log.debug("Found audio URL in recordings[0] as string: {}", url);
                    return url;
                }
            } else if (firstRecording instanceof Map) {
                Map<String, Object> recordingMap = (Map<String, Object>) firstRecording;
                String[] urlFields = {"url", "link", "href"};
                for (String field : urlFields) {
                    Object urlValue = recordingMap.get(field);
                    if (urlValue instanceof String) {
                        String url = (String) urlValue;
                        if (url.startsWith("http") && !url.isEmpty()) {
                            log.debug("Found audio URL in recordings[0].{}: {}", field, url);
                            return url;
                        }
                    }
                }
            }
        }

        // Last resort: Look for any URL-like string value that might be an audio file
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.startsWith("http") && (
                    value.endsWith(".mp3") || value.endsWith(".wav") || value.endsWith(".m4a") || 
                    value.contains("recording") || value.contains("audio"))) {
                    log.debug("Found potential audio URL in field '{}': {}", entry.getKey(), value);
                    return value;
                }
            }
        }

        log.debug("No audio URL found in Map");
        return null;
    }



    /**
     * Check a JSON node for a specific field that contains a URL
     * @param node The JSON node to check
     * @param fieldName The field name to look for
     * @return The URL if found, null otherwise
     */
    private static String checkNode(JsonNode node, String fieldName) {
        if (node.has(fieldName) && node.get(fieldName).isTextual()) {
            String value = node.get(fieldName).asText();
            if (value != null && value.startsWith("http")) {
                return value;
            }
        }
        return null;
    }

    /**
     * Extract audio URL using regex patterns
     * @param json The JSON string to search with regex
     * @return The first audio URL found, or null if none found
     */
    private static String extractWithRegex(String json) {
        String audioUrl = null;

        // Try with quotes first (more precise)
        try {
            Pattern pattern = Pattern.compile(
                "\"((?:recordingUrl|recording_url|audioUrl|audio_url|mediaUrl|media_url))\"\\s*:\\s*\"(https?://[^\"]+)\"");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                audioUrl = matcher.group(2);
                log.debug("Found audio URL using standard JSON pattern: {}", audioUrl);
                return audioUrl;
            }
        } catch (Exception e) {
            log.warn("Error with standard JSON pattern extraction: {}", e.getMessage());
        }

        // Try more flexible pattern without strict quotes
        try {
            Pattern pattern = Pattern.compile(
                "(recordingUrl|recording_url|audioUrl|audio_url|mediaUrl|media_url)\"?\\s*:\\s*\"?(https?://[^\"\\s,}]+)");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                audioUrl = matcher.group(2);
                log.debug("Found audio URL using flexible pattern: {}", audioUrl);
                return audioUrl;
            }
        } catch (Exception e) {
            log.warn("Error with flexible pattern extraction: {}", e.getMessage());
        }

        // Last resort: find any URL that looks like an audio file
        try {
            Pattern pattern = Pattern.compile("(https?://[^\"\\s,}]+\\.(?:mp3|wav|m4a|ogg|mp4))");
            Matcher matcher = pattern.matcher(json);

            if (matcher.find()) {
                audioUrl = matcher.group(1);
                log.debug("Found audio URL using file extension pattern: {}", audioUrl);
                return audioUrl;
            }
        } catch (Exception e) {
            log.warn("Error with file extension pattern extraction: {}", e.getMessage());
        }

        log.debug("No audio URL found in JSON string");
        return null;
    }


    /**
     * Extract audio URL from a String that might contain a direct URL.
     *
     * @param value The string to check
     * @return The extracted URL or null if not valid
     */
    public static String extractFromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Check if the string is a direct URL
        if (value.startsWith("http") && (
            value.endsWith(".mp3") || value.endsWith(".wav") || value.endsWith(".m4a") || 
            value.contains("recording") || value.contains("audio"))) {
            return value;
        }

        return null;
    }

    /**
     * Extract audio URL from various data types (alias for extractFromJson for backward compatibility)
     *
     * @param data The data to extract URL from (String, Map, or JSON string)
     * @return The extracted URL or null if not found
     */
    public static String extractAudioUrl(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        // Try to parse as JSON first
        String result = extractFromJson(data);
        if (result != null) {
            return result;
        }

        // If not JSON, try as direct string
        return extractFromString(data);
    }
}
