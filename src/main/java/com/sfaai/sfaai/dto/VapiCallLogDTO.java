package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO representing a parsed Vapi call log from webhook
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "rawPayload") // Exclude rawPayload from toString to avoid large log outputs
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiCallLogDTO {
    private String callId;
    private String assistantId;
    private String assistantName; // Name of the Vapi assistant
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String audioUrl;
    private String transcriptText;
    private Integer duration; // Duration in seconds
    private String phoneNumber; // Caller's phone number if available
    private String callerId; // Caller ID if available
    private Boolean isInbound; // Whether the call was inbound or outbound

    @Builder.Default
    private List<MessageDTO> messages = new ArrayList<>();
    private String rawPayload; // Minimal version for debugging

    /**
     * Calculate the duration in seconds between start and end time
     * @return Duration in seconds or null if times are not available
     */
    public Integer getDurationInSeconds() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return duration; // Fall back to stored duration if times unavailable
    }

    /**
     * DTO for representing individual messages in the call transcript
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageDTO {
        private String role; // "assistant" or "user"
        private String content;
        private LocalDateTime timestamp;
        private Double confidence; // Speech recognition confidence score (0-1)
        private String messageType; // Type of message (e.g., "speech", "function_call")
        private Map<String, Object> metadata; // Additional message metadata

        /**
         * Check if this message is from the user
         * @return true if the role is "user", false otherwise
         */
        public boolean isFromUser() {
            return "user".equalsIgnoreCase(role);
        }

        /**
         * Check if this message is from the assistant
         * @return true if the role is "assistant", false otherwise
         */
        public boolean isFromAssistant() {
            return "assistant".equalsIgnoreCase(role);
        }
    }
}
