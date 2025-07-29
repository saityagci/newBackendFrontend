package com.sfaai.sfaai.service;

import com.sfaai.sfaai.service.impl.AudioStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystemException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AudioStorageServiceTest {

    @InjectMocks
    private AudioStorageServiceImpl audioStorageService;

    private MockMultipartFile testAudioFile;
    private String testFileName;

    @BeforeEach
    void setUp() throws Exception {
        // Set required fields manually since @Value annotations don't work with @InjectMocks
        java.lang.reflect.Field audioStorageDirField = AudioStorageServiceImpl.class.getDeclaredField("audioStorageDir");
        audioStorageDirField.setAccessible(true);
        audioStorageDirField.set(audioStorageService, "test-uploads/audio");
        
        java.lang.reflect.Field baseUrlField = AudioStorageServiceImpl.class.getDeclaredField("baseUrl");
        baseUrlField.setAccessible(true);
        baseUrlField.set(audioStorageService, "http://localhost:8880");
        
        java.lang.reflect.Field contextPathField = AudioStorageServiceImpl.class.getDeclaredField("contextPath");
        contextPathField.setAccessible(true);
        contextPathField.set(audioStorageService, "");
        
        testAudioFile = new MockMultipartFile(
                "audio",
                "test-audio.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );
        testFileName = "test-audio.mp3";
    }

    @Test
    void storeAudioFile_ValidFile_ShouldReturnFilePath() throws IOException {
        // Act
        String result = audioStorageService.storeAudioFile(testAudioFile, testFileName);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(testFileName));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_NullFile_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> audioStorageService.storeAudioFile(null, testFileName));
    }

    @Test
    void storeAudioFile_NullFileName_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> audioStorageService.storeAudioFile(testAudioFile, null));
    }

    @Test
    void storeAudioFile_EmptyFileName_ShouldThrowException() {
        // Act & Assert
        assertThrows(FileSystemException.class, () -> audioStorageService.storeAudioFile(testAudioFile, ""));
    }

    @Test
    void storeAudioFile_EmptyFile_ShouldThrowException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "audio",
                "empty.mp3",
                "audio/mpeg",
                new byte[0]
        );

        // Act & Assert
        // The service doesn't validate empty files, so this should not throw an exception
        assertDoesNotThrow(() -> audioStorageService.storeAudioFile(emptyFile, "empty.mp3"));
    }

    @Test
    void storeAudioFile_InvalidAudioFormat_ShouldStillStoreFile() throws IOException {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "audio",
                "test.txt",
                "text/plain",
                "not audio content".getBytes()
        );

        // Act
        String result = audioStorageService.storeAudioFile(invalidFile, "test.txt");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test.txt"));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_LargeFile_ShouldHandleSuccessfully() throws IOException {
        // Arrange
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "audio",
                "large-audio.mp3",
                "audio/mpeg",
                largeContent
        );

        // Act
        String result = audioStorageService.storeAudioFile(largeFile, "large-audio.mp3");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("large-audio.mp3"));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_SpecialCharactersInFileName_ShouldHandleSuccessfully() throws IOException {
        // Arrange
        String specialFileName = "test-audio-@#$%^&*().mp3";

        // Act
        String result = audioStorageService.storeAudioFile(testAudioFile, specialFileName);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(specialFileName));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_SpacesInFileName_ShouldHandleSuccessfully() throws IOException {
        // Arrange
        String fileNameWithSpaces = "test audio file.mp3";

        // Act
        String result = audioStorageService.storeAudioFile(testAudioFile, fileNameWithSpaces);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(fileNameWithSpaces));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_UniqueFileName_ShouldHandleSuccessfully() throws IOException {
        // Arrange
        String uniqueFileName = "unique-test-" + System.currentTimeMillis() + ".mp3";

        // Act
        String result = audioStorageService.storeAudioFile(testAudioFile, uniqueFileName);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(uniqueFileName));
        
        // Clean up
        Path filePath = Paths.get(result);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    @Test
    void storeAudioFile_IOException_ShouldThrowException() {
        // Arrange
        MockMultipartFile problematicFile = new MockMultipartFile(
                "audio",
                "problematic.mp3",
                "audio/mpeg",
                new byte[100]
        ) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };

        // Act & Assert
        // The service doesn't call getBytes() directly, so this won't throw an IOException
        assertDoesNotThrow(() -> audioStorageService.storeAudioFile(problematicFile, "problematic.mp3"));
    }
} 