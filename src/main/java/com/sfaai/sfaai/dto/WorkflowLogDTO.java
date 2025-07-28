package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WorkflowLogDTO {
    private Long id;

    @NotNull(message = "Agent ID is required")
    private Long agentId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long voiceLogId;   // Optional, can be null

    @NotBlank(message = "Workflow name is required")
    private String workflowName;

    private String inputData;
    private String outputData;
    private String status;
    private LocalDateTime createdAt;
}