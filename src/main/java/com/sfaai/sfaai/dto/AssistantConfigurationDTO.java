package com.sfaai.sfaai.dto;

import com.sfaai.sfaai.entity.AssistantConfiguration;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AssistantConfigurationDTO {

    private Long id;
    private String assistantId;
    private String subject;
    private String description;
    private AssistantConfiguration.Status status;
    private String clientId;
    private String clientEmail;
    private String clientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssistantDocumentDTO> documents;
} 