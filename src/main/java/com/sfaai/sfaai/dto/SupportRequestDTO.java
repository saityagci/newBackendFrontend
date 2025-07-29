package com.sfaai.sfaai.dto;

import com.sfaai.sfaai.entity.SupportRequest;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SupportRequestDTO {
    
    private Long id;
    private String userId;
    private String userEmail;
    private String userName;
    private String subject;
    private String message;
    private SupportRequest.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 