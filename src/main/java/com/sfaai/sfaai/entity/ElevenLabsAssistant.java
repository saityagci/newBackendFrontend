package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "elevenlabs_assistant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElevenLabsAssistant {

    @Id
    @Column(name = "assistant_id")
    private String assistantId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "voice_id")
    private String voiceId;

    @Column(name = "voice_name")
    private String voiceName;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}
