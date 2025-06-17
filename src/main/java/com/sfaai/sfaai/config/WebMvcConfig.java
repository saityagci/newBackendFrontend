package com.sfaai.sfaai.config;


import com.sfaai.sfaai.interceptor.RateLimitingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the application
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${spring.web.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${spring.web.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${spring.web.cors.max-age:3600}")
    private long maxAge;

    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;

    /**
     * CORS configuration is handled by SecurityConfig
     * This is commented out to avoid conflicts
     */
    // CORS configuration is now in SecurityConfig
    // This is kept commented as reference
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowCredentials(true)
                .maxAge(maxAge);
    }
    */

    /**
     * Register interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/public/**");
    }
}
