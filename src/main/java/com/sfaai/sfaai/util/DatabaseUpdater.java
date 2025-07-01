package com.sfaai.sfaai.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Utility to fix existing audio URLs in the database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseUpdater {

    private final JdbcTemplate jdbcTemplate;

    @Value("${audio.base-url:http://localhost:8880}")
    private String baseUrl;

    /**
     * Update existing audio URLs in the database to use the correct base URL
     * This can be run manually when needed to fix historical data
     * 
     * @param oldBaseUrlPattern Pattern to match and replace (e.g., "http://localhost:8080")
     * @return Number of records updated
     */
    public int updateAudioUrls(String oldBaseUrlPattern) {
        String sql = "UPDATE voice_log SET audio_url = REPLACE(audio_url, ?, ?) WHERE audio_url LIKE ?";
        int updated = jdbcTemplate.update(sql, oldBaseUrlPattern, baseUrl, oldBaseUrlPattern + "%");
        log.info("Updated {} voice log records with new audio URL base {}", updated, baseUrl);
        return updated;
    }

    /**
     * Fix common known URL patterns
     * @return Total number of records updated
     */
    public int fixCommonUrlPatterns() {
        int total = 0;
        total += updateAudioUrls("http://localhost:8080");
        return total;
    }
}
