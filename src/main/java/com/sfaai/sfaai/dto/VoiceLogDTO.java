package com.sfaai.sfaai.dto;

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
    private Long agentId;
    private Long clientId;
    private String provider;
    private String externalCallId;
    private String externalAgentId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String audioUrl;
    private String transcript;
    private String rawPayload; //  for debugging or admin use
    private LocalDateTime createdAt;
}