package com.sfaai.sfaai.util;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.entity.VapiAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class to provide fallback values for missing firstMessage field
 */
@Component
@Slf4j
public class FirstMessageFallbackAdapter {

    /**
     * Apply a fallback message if firstMessage is null
     * @param assistant The assistant to process
     */
    public void applyFallbackMessage(VapiAssistantDTO assistant) {
        if (assistant.getFirstMessage() == null) {
            log.debug("Applying fallback firstMessage for assistant ID: {}", assistant.getAssistantId());

            // Generate a default message based on assistant name
            String fallbackMessage = generateDefaultMessage(assistant.getName());
            assistant.setFirstMessage(fallbackMessage);

            log.debug("Applied fallback firstMessage: {}", fallbackMessage);
        }
    }

    /**
     * Apply a fallback message if firstMessage is null
     * @param assistant The assistant entity to process
     */
    public void applyFallbackMessage(VapiAssistant assistant) {
        if (assistant.getFirstMessage() == null) {
            log.debug("Applying fallback firstMessage for assistant entity ID: {}", assistant.getAssistantId());

            // Generate a default message based on assistant name
            String fallbackMessage = generateDefaultMessage(assistant.getName());
            assistant.setFirstMessage(fallbackMessage);

            log.debug("Applied fallback firstMessage to entity: {}", fallbackMessage);
        }
    }

    /**
     * Generate a default first message based on assistant name
     * @param name The assistant name
     * @return A generated first message
     */
    private String generateDefaultMessage(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello! How can I assist you today?";
        }

        return "Hello! I'm " + name + ". How can I assist you today?";
    }
}
