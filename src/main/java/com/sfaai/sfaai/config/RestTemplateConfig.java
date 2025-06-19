package com.sfaai.sfaai.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


/**
 * Configuration for RestTemplate
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create RestTemplate bean for HTTP requests
     * @return Configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
