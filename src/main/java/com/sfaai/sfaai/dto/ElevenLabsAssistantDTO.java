package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElevenLabsAssistantDTO {

    @JsonProperty("agent_id")
    private String assistantId;

    private String name;

    private String firstMessage;
    private String language;
    private String voiceProvider;
    private String voiceId;
    private String modelProvider;
    private String modelName;
    private String transcriberProvider;
    private String transcriberModel;
    private String transcriberLanguage;
    private String prompt;
    private String knowledgeBaseIds;
    private String conversationConfig;
    private String syncStatus;

    private String description;

    @JsonProperty("voice_name")
    private String voiceName;

    @JsonProperty("model_id")
    private String modelId;

    private String rawData;

    // Store all additional properties from the full API response
    @JsonAnySetter
    private Map<String, Object> details = new java.util.HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getDetails() {
        return details;
    }
}
