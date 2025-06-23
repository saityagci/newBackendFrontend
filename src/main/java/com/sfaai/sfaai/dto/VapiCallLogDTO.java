package com.sfaai.sfaai.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a parsed Vapi call log from webhook
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VapiCallLogDTO {
    private String callId;
    private String assistantId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String audioUrl;
    private String transcriptText;

    @Builder.Default
    private List<MessageDTO> messages = new ArrayList<>();
    private String rawPayload;

    /**
     * DTO for representing individual messages in the call
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class MessageDTO {
        private String role; // "assistant" or "user"
        private String content;
        private LocalDateTime timestamp;
    }
}
