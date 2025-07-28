package com.sfaai.sfaai.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson configuration for JSON serialization/deserialization
 */
@Configuration
@Slf4j
public class JacksonConfig {

    /**
     * Creates and configures the ObjectMapper for the application
     * @return Configured ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule for proper LocalDateTime handling
        mapper.registerModule(new JavaTimeModule());

        // Configure for camelCase field names (default)
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // Additional configuration
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Debug mapping
        debugMappingConfiguration(mapper);

        return mapper;
    }

    /**
     * Tests and logs the JSON field mapping configuration
     */
    private void debugMappingConfiguration(ObjectMapper mapper) {
        try {
            // Test snake_case conversion
            TestDTO dto = new TestDTO();
            dto.setFirstMessage("Test message");

            String json = mapper.writeValueAsString(dto);
            log.debug("JACKSON CONFIG TEST: Object with firstMessage serialized as: {}", json);

            // Check if it contains the correct field name
            boolean hasSnakeCase = json.contains("first_message");
            boolean hasCamelCase = json.contains("firstMessage");

            log.debug("JACKSON CONFIG TEST: JSON contains 'first_message': {}", hasSnakeCase);
            log.debug("JACKSON CONFIG TEST: JSON contains 'firstMessage': {}", hasCamelCase);

            // Test deserialization both ways
            String snakeCaseJson = "{\"first_message\":\"From snake_case\"}";
            String camelCaseJson = "{\"firstMessage\":\"From camelCase\"}";

            TestDTO fromSnakeCase = mapper.readValue(snakeCaseJson, TestDTO.class);
            TestDTO fromCamelCase = mapper.readValue(camelCaseJson, TestDTO.class);

            log.debug("JACKSON CONFIG TEST: Deserialized from snake_case: {}", fromSnakeCase.getFirstMessage());
            log.debug("JACKSON CONFIG TEST: Deserialized from camelCase: {}", fromCamelCase.getFirstMessage());

        } catch (Exception e) {
            log.error("Error testing Jackson configuration: {}", e.getMessage(), e);
        }
    }

    /**
     * Test class for JSON mapping
     */
    static class TestDTO {
        private String firstMessage;

        public String getFirstMessage() {
            return firstMessage;
        }

        public void setFirstMessage(String firstMessage) {
            this.firstMessage = firstMessage;
        }
    }
}
