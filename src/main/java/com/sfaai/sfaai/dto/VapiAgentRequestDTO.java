package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a Vapi voice agent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VapiAgentRequestDTO {

    @NotBlank(message = "Agent name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Greeting is required")
    @Size(max = 1000, message = "Greeting must not exceed 1000 characters")
    private String greeting;

    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    private String language;

    // Optional client ID, will be set by the service if not provided
    private Long clientId;
}
