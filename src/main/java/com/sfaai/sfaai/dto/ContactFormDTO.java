package com.sfaai.sfaai.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactFormDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Company is required")
    private String company;

    private String website; // Optional

    @NotBlank(message = "Service is required")
    private String service;

    @NotBlank(message = "Message is required")
    private String message;
} 