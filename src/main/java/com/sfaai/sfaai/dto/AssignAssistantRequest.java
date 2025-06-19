package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a Vapi assistant to a client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignAssistantRequest {

    @NotBlank(message = "Vapi assistant ID is required")
    private String vapiAssistantId;
}
