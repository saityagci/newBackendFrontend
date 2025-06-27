package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.service.AudioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation for storing audio files locally
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class LocalAudioStorageServiceImpl implements AudioStorageService {

    @Value("${app.audio-storage.local-path:uploads/audio}")
    private String storagePath;

    @Value("${app.audio-storage.base-url:http://localhost:8080/audio}")
    private String baseUrl;

    @Override
    public String storeAudioFromUrl(String sourceUrl, String callId) {
        try {
            URL url = new URL(sourceUrl);
            try (InputStream inputStream = url.openStream()) {
                String fileName = generateFileName(callId);
                return saveToFile(inputStream, fileName);
            }
        } catch (IOException e) {
            log.error("Failed to download audio from URL: {}", sourceUrl, e);
            return sourceUrl; // Return original URL if download fails
        }
    }

    @Override
    public String storeAudio(InputStream inputStream, String callId, String contentType) {
        try {
            String extension = getExtensionFromContentType(contentType);
            String fileName = generateFileName(callId) + extension;
            return saveToFile(inputStream, fileName);
        } catch (IOException e) {
            log.error("Failed to store audio file for call: {}", callId, e);
            return null;
        }
    }

    @Override
    public String getPublicUrl(String storagePath) {
        if (storagePath == null) return null;

        // If the storage path is already a URL, return it
        if (storagePath.startsWith("http")) {
            return storagePath;
        }

        // Extract just the filename from the path
        String fileName = Paths.get(storagePath).getFileName().toString();
        return baseUrl + "/" + fileName;
    }

    /**
     * Generate a unique filename for the audio file
     * @param callId Call ID
     * @return Generated filename
     */
    private String generateFileName(String callId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "call_" + callId + "_" + timestamp;
    }

    /**
     * Save input stream to a file
     * @param inputStream Input stream to save
     * @param fileName Name of the file to save
     * @return Path to the saved file
     * @throws IOException If file operations fail
     */
    private String saveToFile(InputStream inputStream, String fileName) throws IOException {
        // Create storage directory if it doesn't exist
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Path targetPath = Paths.get(storagePath, fileName);
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Saved audio file to: {}", targetPath);
        return targetPath.toString();
    }

    /**
     * Get file extension from content type
     * @param contentType Content type string
     * @return File extension including the dot
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return ".mp3"; // Default

        switch (contentType.toLowerCase()) {
            case "audio/mpeg":
            case "audio/mp3":
                return ".mp3";
            case "audio/wav":
            case "audio/x-wav":
                return ".wav";
            case "audio/ogg":
                return ".ogg";
            case "audio/m4a":
            case "audio/x-m4a":
                return ".m4a";
            default:
                return ".mp3"; // Default to mp3 if unknown
        }
    }
}
