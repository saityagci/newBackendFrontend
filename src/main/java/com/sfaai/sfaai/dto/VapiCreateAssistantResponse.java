package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Vapi assistant creation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiCreateAssistantResponse {

    @JsonProperty("id")
    private String assistantId;

    private String status;

    // Alias for assistantId to support both naming conventions
    public String getId() {
        return assistantId;
    }

    public void setId(String id) {
        this.assistantId = id;
    }

    // Alias for status to support both naming conventions
    public String getName() {
        return status;
    }

    public void setName(String name) {
        this.status = name;
    }

    // You can add more fields as needed from the Vapi API response
}
