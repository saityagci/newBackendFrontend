package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for external Vapi API request
 * This matches the format expected by the Vapi API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VapiExternalRequestDTO {

    private String name;
    private String greeting;
    private String language;

    // Additional Vapi-specific fields
    private String voice_id;   // Optional voice ID
    private Boolean public_agent;
    private Boolean handoff_url;

    // Other Vapi configuration options can be added here
}
