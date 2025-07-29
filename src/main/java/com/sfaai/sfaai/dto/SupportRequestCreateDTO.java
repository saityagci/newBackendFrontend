package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SupportRequestCreateDTO {
    
    @NotBlank(message = "Subject is required")
    @Size(max = 500, message = "Subject must not exceed 500 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "User email is required")
    @Email(message = "Please provide a valid email address")
    private String userEmail;
    
    private String userName;
} 