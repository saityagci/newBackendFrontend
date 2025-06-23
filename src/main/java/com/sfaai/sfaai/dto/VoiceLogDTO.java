package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VoiceLogDTO {
    private Long id;

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Provider is required")
    private String provider;

    private String externalCallId;
    private String externalAgentId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String audioUrl;
    private String transcript;
    private String rawPayload; // for debugging or admin use
    private String conversationData; // structured conversation data
    private LocalDateTime createdAt;
}