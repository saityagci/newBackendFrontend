package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type; // e.g., "VAPI", "n8n", etc.
    private String status; // e.g., "ACTIVE", "INACTIVE"
    private Long clientId; // Foreign key to Client

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
}