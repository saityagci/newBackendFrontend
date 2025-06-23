package com.sfaai.sfaai.service;

import java.io.InputStream;

/**
 * Service interface for storing and retrieving audio files
 */
public interface AudioStorageService {

    /**
     * Store audio file from a URL
     * @param sourceUrl URL of the audio file to download
     * @param callId The call ID to use in the filename
     * @return URL or path to the stored file
     */
    String storeAudioFromUrl(String sourceUrl, String callId);

    /**
     * Store audio file from a binary input stream
     * @param inputStream Input stream containing the audio data
     * @param callId The call ID to use in the filename
     * @param contentType The content type of the audio (e.g., "audio/mpeg")
     * @return URL or path to the stored file
     */
    String storeAudio(InputStream inputStream, String callId, String contentType);

    /**
     * Get public URL for accessing the audio file
     * @param storagePath Path where the audio is stored
     * @return Public URL for the audio
     */
    String getPublicUrl(String storagePath);
}
