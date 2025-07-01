package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElevenLabsAssistantDTO {

    @JsonProperty("agent_id")
    private String assistantId;

    private String name;

    private String description;

    @JsonProperty("voice_id")
    private String voiceId;

    @JsonProperty("voice_name")
    private String voiceName;

    @JsonProperty("model_id")
    private String modelId;

    private String rawData;
}
