
package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceLog {

    public enum Status {
        INITIATED, RINGING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }

    public enum Provider {
        VAPI, ELEVENLABS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_call_id")
    private String externalCallId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "duration_minutes")
    private Double durationMinutes;

    @Column(name = "external_agent_id")
    private String externalAgentId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id")
    private VapiAssistant vapiAssistant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elevenlabs_assistant_id")
    private ElevenLabsAssistant elevenLabsAssistant;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "conversation_data", columnDefinition = "TEXT")
    private String conversationData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Calculated field for duration in seconds
    public Integer getDuration() {
        if (startedAt != null && endedAt != null) {
            return (int) java.time.Duration.between(startedAt, endedAt).getSeconds();
        }
        return null;
    }

    // Calculated field for duration in minutes if not already set
    public Double getDurationMinutes() {
        if (durationMinutes != null) {
            return durationMinutes;
        }

        if (startedAt != null && endedAt != null) {
            long seconds = java.time.Duration.between(startedAt, endedAt).getSeconds();
            return seconds / 60.0;
        }
        return null;
    }
}
