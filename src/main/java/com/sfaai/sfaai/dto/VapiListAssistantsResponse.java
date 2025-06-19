package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Vapi list assistants response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VapiListAssistantsResponse {

    private List<VapiAssistantDTO> assistants;
    private Integer total;
    private Integer page;
    private Integer limit;
}
