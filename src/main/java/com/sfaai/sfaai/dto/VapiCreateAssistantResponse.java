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

    // You can add more fields as needed from the Vapi API response
}
