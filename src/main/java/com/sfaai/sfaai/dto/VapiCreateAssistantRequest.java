package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a Vapi assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VapiCreateAssistantRequest {

    @NotNull(message = "Transcriber is required")
    @Valid
    private Transcriber transcriber;

    @NotNull(message = "Model is required")
    @Valid
    private Model model;

    @NotNull(message = "Voice is required")
    @Valid
    private Voice voice;

    @NotBlank(message = "First message is required")
    private String firstMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transcriber {
        @NotBlank(message = "Provider is required")
        private String provider;

        @NotBlank(message = "Model is required")
        private String model;

        @NotBlank(message = "Language is required")
        private String language;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Model {
        @NotBlank(message = "Provider is required")
        private String provider;

        @NotBlank(message = "Model is required")
        private String model;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Voice {
        @NotBlank(message = "Provider is required")
        private String provider;

        @NotBlank(message = "Voice ID is required")
        private String voiceId;
    }
}
