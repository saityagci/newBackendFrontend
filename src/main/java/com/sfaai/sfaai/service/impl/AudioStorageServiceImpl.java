package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.service.AudioStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for storing and retrieving audio files
 */
@Service
@Slf4j
public class AudioStorageServiceImpl implements AudioStorageService {

    @Value("${audio.storage.dir:uploads/audio}")
    private String audioStorageDir;

    @Value("${audio.base-url:http://localhost:8880}")
    private String baseUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public String storeAudioFromUrl(String sourceUrl, String callId) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            log.warn("Cannot store audio from empty URL");
            return null;
        }

        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(audioStorageDir);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                log.info("Created audio storage directory: {}", storageDir);
            }

            // Generate a filename based on call ID and timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Extract file extension manually
            String originalExtension = "";
            int lastDotIndex = sourceUrl.lastIndexOf('.');
            int lastSlashIndex = Math.max(sourceUrl.lastIndexOf('/'), sourceUrl.lastIndexOf('\\'));

            if (lastDotIndex > lastSlashIndex && lastDotIndex != -1) {
                originalExtension = sourceUrl.substring(lastDotIndex + 1);
                // Remove any query parameters that might be in the URL
                int queryParamIndex = originalExtension.indexOf('?');
                if (queryParamIndex != -1) {
                    originalExtension = originalExtension.substring(0, queryParamIndex);
                }
            }

            String extension = originalExtension.isEmpty() ? "mp3" : originalExtension;
            String filename = "call_" + callId + "_" + timestamp + "." + extension;
            Path destinationPath = storageDir.resolve(filename);

            // Download the file
            log.info("Downloading audio from {} to {}", sourceUrl, destinationPath);
            URL url = new URL(sourceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("Failed to download audio file, response code: {}", responseCode);
                return null;
            }

            try (InputStream inputStream = connection.getInputStream();
                 ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                 FileOutputStream fileOutputStream = new FileOutputStream(destinationPath.toFile())) {

                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }

            log.info("Successfully downloaded audio file to {}", destinationPath);
            return destinationPath.toString();

        } catch (IOException e) {
            log.error("Error storing audio file from URL: {}", sourceUrl, e);
            return null;
        }
    }

    @Override
    public String storeAudio(InputStream inputStream, String callId, String contentType) {
        return "";
    }

    public String storeAudioFile(org.springframework.web.multipart.MultipartFile file, String fileName) throws IOException {
        if (file == null || fileName == null) {
            throw new IllegalArgumentException("File and fileName cannot be null");
        }

        // Create storage directory if it doesn't exist
        Path storageDir = Paths.get(audioStorageDir);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
            log.info("Created audio storage directory: {}", storageDir);
        }

        Path destinationPath = storageDir.resolve(fileName);
        
        // Save the file
        file.transferTo(destinationPath.toFile());
        
        log.info("Successfully stored audio file to {}", destinationPath);
        return destinationPath.toString();
    }

    @Override
    public String getPublicUrl(String storedPath) {
        if (storedPath == null || storedPath.isEmpty()) {
            return null;
        }

        // If the stored path is already a URL, return it
        if (storedPath.startsWith("http")) {
            return storedPath;
        }

        // Extract the filename from the path
        File file = new File(storedPath);
        String filename = file.getName();

        // Construct audio path
        String audioPath = "/audio/" + filename;

        // Build URL using baseUrl, properly handling trailing slashes
        if (baseUrl.endsWith("/")) {
            return baseUrl + (contextPath.startsWith("/") ? contextPath.substring(1) : contextPath) + 
                  (audioPath.startsWith("/") ? audioPath : "/" + audioPath);
        } else {
            return baseUrl + 
                  (contextPath.isEmpty() ? "" : (contextPath.startsWith("/") ? contextPath : "/" + contextPath)) + 
                  (audioPath.startsWith("/") ? audioPath : "/" + audioPath);
        }
    }
}
