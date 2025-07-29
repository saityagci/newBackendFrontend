package com.sfaai.sfaai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioUrlExtractorTest {

    private AudioUrlExtractor audioUrlExtractor;

    @BeforeEach
    void setUp() {
        audioUrlExtractor = new AudioUrlExtractor();
    }

    @Test
    void extractAudioUrl_ValidJsonWithAudioUrl_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"audioUrl\": \"https://example.com/audio.mp3\", \"otherField\": \"value\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_ValidJsonWithDifferentCase_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"audioURL\": \"https://example.com/audio.mp3\", \"otherField\": \"value\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_ValidJsonWithAudioUrlFirst_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"audioUrl\": \"https://example.com/audio.mp3\", \"field1\": \"value1\", \"field2\": \"value2\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_ValidJsonWithAudioUrlLast_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"field1\": \"value1\", \"field2\": \"value2\", \"audioUrl\": \"https://example.com/audio.mp3\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_ValidJsonWithAudioUrlMiddle_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"field1\": \"value1\", \"audioUrl\": \"https://example.com/audio.mp3\", \"field2\": \"value2\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_JsonWithoutAudioUrl_ShouldReturnNull() {
        // Arrange
        String json = "{\"field1\": \"value1\", \"field2\": \"value2\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_EmptyJson_ShouldReturnNull() {
        // Arrange
        String json = "{}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_NullJson_ShouldReturnNull() {
        // Act
        String result = audioUrlExtractor.extractAudioUrl(null);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_EmptyString_ShouldReturnNull() {
        // Act
        String result = audioUrlExtractor.extractAudioUrl("");

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_BlankString_ShouldReturnNull() {
        // Act
        String result = audioUrlExtractor.extractAudioUrl("   ");

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_InvalidJson_ShouldReturnNull() {
        // Arrange
        String invalidJson = "invalid json string";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(invalidJson);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_MalformedJson_ShouldReturnNull() {
        // Arrange
        String malformedJson = "{\"audioUrl\": \"https://example.com/audio.mp3\", \"field1\":}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(malformedJson);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_JsonWithNullAudioUrl_ShouldReturnNull() {
        // Arrange
        String json = "{\"audioUrl\": null, \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertNull(result);
    }

    @Test
    void extractAudioUrl_JsonWithEmptyAudioUrl_ShouldReturnEmptyString() {
        // Arrange
        String json = "{\"audioUrl\": \"\", \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("", result);
    }

    @Test
    void extractAudioUrl_JsonWithBlankAudioUrl_ShouldReturnBlankString() {
        // Arrange
        String json = "{\"audioUrl\": \"   \", \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("   ", result);
    }

    @Test
    void extractAudioUrl_JsonWithComplexAudioUrl_ShouldReturnAudioUrl() {
        // Arrange
        String complexUrl = "https://api.example.com/v1/audio/12345?token=abc123&format=mp3&quality=high";
        String json = "{\"audioUrl\": \"" + complexUrl + "\", \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals(complexUrl, result);
    }

    @Test
    void extractAudioUrl_JsonWithSpecialCharacters_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"audioUrl\": \"https://example.com/audio%20file.mp3\", \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio%20file.mp3", result);
    }

    @Test
    void extractAudioUrl_JsonWithNestedObjects_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"data\": {\"audioUrl\": \"https://example.com/audio.mp3\"}, \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_JsonWithArrays_ShouldReturnAudioUrl() {
        // Arrange
        String json = "{\"items\": [{\"audioUrl\": \"https://example.com/audio.mp3\"}], \"field1\": \"value1\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio.mp3", result);
    }

    @Test
    void extractAudioUrl_JsonWithMultipleAudioUrls_ShouldReturnFirstAudioUrl() {
        // Arrange
        String json = "{\"audioUrl\": \"https://example.com/audio1.mp3\", \"audioURL\": \"https://example.com/audio2.mp3\"}";

        // Act
        String result = audioUrlExtractor.extractAudioUrl(json);

        // Assert
        assertEquals("https://example.com/audio1.mp3", result);
    }
} 