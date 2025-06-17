package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogWebhookDTO;

/**
 * Mapper for converting webhook DTO to create DTO
 */
public class VoiceLogWebhookMapper {

    /**
     * Convert webhook DTO to create DTO
     * 
     * @param webhookDTO the webhook DTO
     * @return the create DTO
     */
    public static VoiceLogCreateDTO toCreateDTO(VoiceLogWebhookDTO webhookDTO) {
        if (webhookDTO == null) {
            return null;
        }

        return VoiceLogCreateDTO.builder()
                .agentId(webhookDTO.getAgentId())
                .clientId(webhookDTO.getClientId())
                .provider(webhookDTO.getProvider())
                .externalCallId(webhookDTO.getCallId())
                .externalAgentId(webhookDTO.getAgentExternalId())
                .startedAt(webhookDTO.getCallStartTime())
                .endedAt(webhookDTO.getCallEndTime())
                .audioUrl(webhookDTO.getRecordingUrl())
                .transcript(webhookDTO.getCallTranscript())
                .rawPayload(webhookDTO.getRawData())
                .build();
    }
}
