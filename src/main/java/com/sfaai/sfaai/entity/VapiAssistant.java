package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vapi_assistant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VapiAssistant {
    @Id
    @Column(length = 64)
    private String assistantId;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "sync_status")
    private String syncStatus;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column
    private String status;

    @Column(name = "first_message")
    private String firstMessage;

    @Column(name = "voice_provider")
    private String voiceProvider;

    @Column(name = "voice_id")
    private String voiceId;

    @Column(name = "model_provider")
    private String modelProvider;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "transcriber_provider")
    private String transcriberProvider;

    @Column(name = "transcriber_model")
    private String transcriberModel;

    @Column(name = "transcriber_language")
    private String transcriberLanguage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
