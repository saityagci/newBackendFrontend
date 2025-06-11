package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to your Agent
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    // Optionally link to Client
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // Voice provider: vapi, elevenlabs, etc.
    private String provider;

    // Call/session ID from the external provider
    private String externalCallId;

    // Optional: the provider's agent id (if needed)
    private String externalAgentId;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private String audioUrl;
    @Lob
    @Column(columnDefinition = "text")
    private String transcript;

    // Optionally: store the raw webhook payload as JSON for debugging
    @Lob
    private String rawPayload;

    private LocalDateTime createdAt;
}