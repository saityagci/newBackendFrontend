package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "elevenlabs_assistant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElevenLabsAssistant {
    @Id
    @Column(name = "assistant_id", length = 64)
    private String assistantId;

    // Alias for assistantId to support both naming conventions
    public String getId() {
        return assistantId;
    }

    public void setId(String id) {
        this.assistantId = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(name = "first_message")
    private String firstMessage;

    @Column(name = "language")
    private String language;

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

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "knowledge_base_ids")
    private String knowledgeBaseIds; // Comma-separated or JSON string

    @Column(name = "conversation_config", columnDefinition = "TEXT")
    private String conversationConfig;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
