package com.sfaai.sfaai.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for API rate limiting
 */
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Cache<String, RateLimiter> rateLimiterCache;
    private final RateLimiter defaultRateLimiter;
    private final boolean rateLimitingEnabled;

    /**
     * Implement rate limiting before controller execution
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitingEnabled) {
            return true;
        }

        // Get client identifier (IP address or API key if available)
        String clientId = getClientIdentifier(request);

        // Get or create rate limiter for this client
        RateLimiter rateLimiter = rateLimiterCache.get(clientId, k -> defaultRateLimiter);

        // Try to acquire permit
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }

        return true;
    }

    /**
     * Get client identifier - either API key or IP address
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // First try to get API key from header or parameter
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api_" + apiKey;
        }

        // Fall back to IP address
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return "ip_" + ipAddress;
    }
}
