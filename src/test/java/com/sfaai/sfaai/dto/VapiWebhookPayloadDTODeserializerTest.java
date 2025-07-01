package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VapiWebhookPayloadDTODeserializerTest {

    @Test
    public void testDeserializer_WithBothRecordingUrls() throws Exception {
        // Setup test JSON with both root-level and nested recordingUrl
        String json = "{\n" +
                "  \"recordingUrl\": \"https://audio.url/abc.wav\",\n" +
                "  \"message\": {\n" +
                "    \"artifact\": {\n" +
                "      \"recordingUrl\": \"https://audio.url/xyz.wav\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // Deserialize
        ObjectMapper mapper = new ObjectMapper();
        VapiWebhookPayloadDTO dto = mapper.readValue(json, VapiWebhookPayloadDTO.class);

        // Verify both URLs are accessible through the DTO
        assertEquals("https://audio.url/abc.wav", dto.getProperties().get("recordingUrl"));
        assertEquals("https://audio.url/xyz.wav", dto.getAudioUrl());
        assertEquals("https://audio.url/abc.wav", dto.getAnyRecordingUrl());

        // Print full results for debugging
        System.out.println("Test results:");
        System.out.println("Properties: " + dto.getProperties());
        System.out.println("Message: " + dto.getMessage());
        System.out.println("getAudioUrl(): " + dto.getAudioUrl());
        System.out.println("getAnyRecordingUrl(): " + dto.getAnyRecordingUrl());
    }

    @Test
    public void testDeserializer_WithRootRecordingUrl() throws Exception {
        // Setup test JSON with only root-level recordingUrl
        String json = "{\n" +
                "  \"recordingUrl\": \"https://audio.url/abc.wav\"\n" +
                "}";

        // Deserialize
        ObjectMapper mapper = new ObjectMapper();
        VapiWebhookPayloadDTO dto = mapper.readValue(json, VapiWebhookPayloadDTO.class);

        // Verify URL is accessible
        assertEquals("https://audio.url/abc.wav", dto.getProperties().get("recordingUrl"));
        assertNull(dto.getAudioUrl());
        assertEquals("https://audio.url/abc.wav", dto.getAnyRecordingUrl());
    }

    @Test
    public void testDeserializer_WithNestedRecordingUrl() throws Exception {
        // Setup test JSON with only nested recordingUrl
        String json = "{\n" +
                "  \"message\": {\n" +
                "    \"artifact\": {\n" +
                "      \"recordingUrl\": \"https://audio.url/xyz.wav\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // Deserialize
        ObjectMapper mapper = new ObjectMapper();
        VapiWebhookPayloadDTO dto = mapper.readValue(json, VapiWebhookPayloadDTO.class);

        // Verify URL is accessible
        assertNull(dto.getProperties().get("recordingUrl"));
        assertEquals("https://audio.url/xyz.wav", dto.getAudioUrl());
        assertEquals("https://audio.url/xyz.wav", dto.getAnyRecordingUrl());
    }

    @Test
    public void testDeserializer_WithAlternativeRecordingUrls() throws Exception {
        // Setup test JSON with alternative URL formats
        String json = "{\n" +
                "  \"call\": {\n" +
                "    \"recordingUrl\": \"https://audio.url/call.wav\"\n" +
                "  },\n" +
                "  \"artifact\": {\n" +
                "    \"recording_url\": \"https://audio.url/artifact.wav\"\n" +
                "  }\n" +
                "}";

        // Deserialize
        ObjectMapper mapper = new ObjectMapper();
        VapiWebhookPayloadDTO dto = mapper.readValue(json, VapiWebhookPayloadDTO.class);

        // Test the getAnyRecordingUrl method to ensure it finds URLs in alternative locations
        assertNotNull(dto.getAnyRecordingUrl());
        assertTrue(dto.getAnyRecordingUrl().contains("audio.url"));
    }
}
