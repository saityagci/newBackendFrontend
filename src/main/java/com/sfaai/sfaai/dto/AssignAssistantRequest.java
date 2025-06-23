package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for assigning a Vapi assistant to a client
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignAssistantRequest {

    @NotBlank(message = "Vapi assistant ID is required")
    @JsonProperty("vapiAssistantId")
    private String vapiAssistantId;
}
