package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for assigning assistants to clients
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignAssistantRequest {

    @NotBlank(message = "Assistant ID is required")
    @JsonProperty("assistantId")
    private String assistantId;

    @JsonProperty("assistantType")
    private String assistantType; // "VAPI" or "ELEVENLABS"

    // Legacy support for Vapi assistants
    @JsonProperty("vapiAssistantId")
    public String getVapiAssistantId() {
        return "VAPI".equals(assistantType) ? assistantId : null;
    }

    public void setVapiAssistantId(String vapiAssistantId) {
        this.assistantId = vapiAssistantId;
        this.assistantType = "VAPI";
    }

    // Support for ElevenLabs assistants
    @JsonProperty("elevenLabsAssistantId")
    public String getElevenLabsAssistantId() {
        return "ELEVENLABS".equals(assistantType) ? assistantId : null;
    }

    public void setElevenLabsAssistantId(String elevenLabsAssistantId) {
        this.assistantId = elevenLabsAssistantId;
        this.assistantType = "ELEVENLABS";
    }
}
