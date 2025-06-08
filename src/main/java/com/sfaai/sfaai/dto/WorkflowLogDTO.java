package com.sfaai.sfaai.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class WorkflowLogDTO {
    private String clientId;
    private String workflowName;
    private String inputJson;
    private String outputJson;
    private LocalDateTime createdAt;
}
