package com.sfaai.sfaai.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration for API rate limiting
 */
@Configuration
@Validated
public class RateLimitingConfig {

    @Value("${rate-limiting.enabled:true}")
    private boolean enabled;

    @Positive(message = "Rate limit must be positive")
    @Value("${rate-limiting.limit:60}")
    private double limit;

    @Value("${rate-limiting.cache.max-size:10000}")
    private long maxCacheSize;

    @Value("${rate-limiting.cache.expire-after-hours:1}")
    private long expireAfterHours;

    /**
     * Creates a cache to store rate limiters per client/IP
     * Cache keys should be client IPs or identifiers
     * @return Cache for rate limiters
     */
    @Bean
    public Cache<String, RateLimiter> rateLimiterCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(expireAfterHours, TimeUnit.HOURS)
                .maximumSize(maxCacheSize)
                .removalListener((key, value, cause) -> {
                    if (value instanceof RateLimiter) {
                        // Cleanup any resources if needed
                    }
                })
                .build();
    }

    /**
     * Creates a RateLimiter factory method for per-client rate limiters
     * Higher limits for development environment (localhost)
     * @return Function to create new rate limiters
     */
    @Bean
    public java.util.function.Supplier<RateLimiter> rateLimiterSupplier() {
        if (isLocalEnvironment()) {
            // Return a very high rate limit for local development (1000 requests per minute)
            return () -> RateLimiter.create(1000 / 60.0);
        }
        return () -> RateLimiter.create(limit / 60.0);
    }

    /**
     * Default RateLimiter bean for general use
     * Higher limits for development environment (localhost)
     * @return Default RateLimiter instance
     */
    @Bean
    public RateLimiter defaultRateLimiter() {
        if (isLocalEnvironment()) {
            // Return a very high rate limit for local development (1000 requests per minute)
            return RateLimiter.create(1000 / 60.0);
        }
        return RateLimiter.create(limit / 60.0);
    }

    /**
     * @return Whether rate limiting is enabled
     */
    @Bean
    public boolean isRateLimitingEnabled() {
        // Optionally disable rate limiting completely in local development
        if (isLocalEnvironment()) {
            return false; // Disable rate limiting entirely for local development
        }
        return enabled;
    }

    /**
     * Helper method to determine if running in local development environment
     * @return true if running on localhost
     */
    private boolean isLocalEnvironment() {
        try {
            // Check if running on localhost
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            return hostAddress.startsWith("127.") || hostAddress.equals("::1") || hostAddress.equals("0:0:0:0:0:0:0:1");
        } catch (UnknownHostException e) {
            return false;
        }
    }
}