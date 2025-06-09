package com.sfaai.sfaai.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AgentDTO {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Long clientId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
