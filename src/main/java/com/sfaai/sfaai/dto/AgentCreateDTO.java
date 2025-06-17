package com.sfaai.sfaai.dto;

import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AgentCreateDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @NotNull(message = "Type is required")
    private AgentType type;

    @NotNull(message = "Status is required")
    private AgentStatus status;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
