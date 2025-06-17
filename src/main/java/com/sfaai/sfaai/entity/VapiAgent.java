package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a Vapi voice agent
 */
@Entity
@Table(name = "vapi_agent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VapiAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "vapi_agent_id", nullable = false, unique = true)
    private String vapiAgentId;  // External Vapi ID

    @NotBlank
    @Size(min = 2, max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String greeting;

    @NotBlank
    @Size(min = 2, max = 10)
    @Column(nullable = false, length = 10)
    private String language;

    @Column(name = "voice_id")
    private String voiceId;

    @Column(name = "public_agent")
    private Boolean publicAgent;

    @NotBlank
    @Column(nullable = false)
    private String status;  // Maps to Vapi agent status

    // Store full Vapi response as JSON
    @Column(name = "vapi_details", columnDefinition = "TEXT")
    private String vapiDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
