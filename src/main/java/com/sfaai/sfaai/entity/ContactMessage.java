package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String role;
    private String company;
    private String website;
    private String service;
    private String message;
    private LocalDateTime submittedAt;
} 