package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationErrorDTO> validationErrors;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String suggestion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String apiName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;

    /**
     * DTO for field validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorDTO {
        private String field;
        private String message;
    }
}
