package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class VoiceLogCreateDTO {
    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotBlank(message = "Assistant ID is required")
    private String assistantId;

    private String externalCallId;
    private String externalAgentId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String audioUrl;
    private String transcript;
    private String rawPayload;
    private String conversationData; // structured conversation data
    private com.sfaai.sfaai.entity.VoiceLog.Status status;
}
