package com.sfaai.sfaai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Vapi voice agent details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VapiAgentResponseDTO {

    private Long id;                // Local database ID
    private String vapiAgentId;     // External Vapi ID
    private String name;
    private String greeting;
    private String language;
    private Long clientId;
    private String status;         // Agent status
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional Vapi-specific fields can be added as needed
    private Object vapiDetails;    // Full Vapi API response (as Object for flexibility)
}
