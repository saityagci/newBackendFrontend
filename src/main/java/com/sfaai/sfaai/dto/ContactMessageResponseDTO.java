package com.sfaai.sfaai.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageResponseDTO {
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