package com.sfaai.sfaai.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import java.util.concurrent.TimeUnit;

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
     * @return Function to create new rate limiters
     */
    @Bean
    public java.util.function.Supplier<RateLimiter> rateLimiterSupplier() {
        return () -> RateLimiter.create(limit / 60.0);
    }

    /**
     * Default RateLimiter bean for general use
     * @return Default RateLimiter instance
     */
    @Bean
    public RateLimiter defaultRateLimiter() {
        return RateLimiter.create(limit / 60.0);
    }

    /**
     * @return Whether rate limiting is enabled
     */
    @Bean
    public boolean isRateLimitingEnabled() {
        return enabled;
    }
}