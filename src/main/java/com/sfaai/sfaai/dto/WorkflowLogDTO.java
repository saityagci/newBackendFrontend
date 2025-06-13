package com.sfaai.sfaai.dto;

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
    private Long agentId;
    private Long clientId;
    private Long voiceLogId;   // Optional, can be null
    private String workflowName;
    private String inputData;
    private String outputData;
    private LocalDateTime createdAt;
}