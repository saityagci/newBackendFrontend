package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElevenLabsListAssistantsResponse {

    @JsonProperty("agents")
    private List<ElevenLabsAssistantDTO> assistants;

    private String cursor;

    @JsonProperty("has_more")
    private boolean hasMore;
}
