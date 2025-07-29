package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

/**
 * DTO for updating client information
 * Fields are optional for partial updates
 */
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ClientUpdateDTO {
    private Long id;

    private String role;

    // Temporarily removed validation to test if it's causing the issue
    private String fullName;

    // Temporarily removed validation to test if it's causing the issue
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    private String apiKey;
    private String vapiAssistantId;
    private List<String> vapiAssistantIds;
} 