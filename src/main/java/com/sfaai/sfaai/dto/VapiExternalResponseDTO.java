package com.sfaai.sfaai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for external Vapi API response
 * This captures the response from the Vapi API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VapiExternalResponseDTO {

    private String id;              // Vapi's agent ID
    private String name;
    private String greeting;
    private String language;
    private String status;
    private String created_at;
    private String updated_at;

    // Additional fields from Vapi response
    private String voice_id;
    private Boolean public_agent;
    private Boolean handoff_url;

    // Add any other fields returned by the Vapi API
}
