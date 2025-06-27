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
@ToString
public class VoiceLogWebhookDTO {
    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Provider is required")
    private String provider;

    private String callId;
    private String agentExternalId;
    private String callStatus;
    private LocalDateTime callStartTime;
    private LocalDateTime callEndTime;
    private String recordingUrl;
    private String callTranscript;
    private String rawData;
}
