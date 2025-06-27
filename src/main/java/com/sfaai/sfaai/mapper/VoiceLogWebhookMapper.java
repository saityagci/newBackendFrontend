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

                        // Map status if available
                        com.sfaai.sfaai.entity.VoiceLog.Status status = null;
                        if (webhookDTO.getCallStatus() != null) {
                            try {
                String callStatus = webhookDTO.getCallStatus().toUpperCase();
                if (callStatus.contains("COMPLET")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.COMPLETED;
                } else if (callStatus.contains("FAIL") || callStatus.contains("ERROR")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.FAILED;
                } else if (callStatus.contains("PROGRESS") || callStatus.contains("ACTIVE")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.IN_PROGRESS;
                } else if (callStatus.contains("RING")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.RINGING;
                } else if (callStatus.contains("CANCEL")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.CANCELLED;
                } else if (callStatus.contains("INIT")) {
                    status = com.sfaai.sfaai.entity.VoiceLog.Status.INITIATED;
                }
                            } catch (Exception e) {
                // Ignore mapping errors
                            }
                        }

                        // Calculate duration minutes if not provided but we have start and end times
                        Float durationMinutes = webhookDTO.getDurationMinutes();
                        if (durationMinutes == null && webhookDTO.getCallStartTime() != null && webhookDTO.getCallEndTime() != null) {
                            long seconds = java.time.Duration.between(webhookDTO.getCallStartTime(), webhookDTO.getCallEndTime()).getSeconds();
                            durationMinutes = seconds / 60.0f;
                        }

                        return VoiceLogCreateDTO.builder()
                .agentId(webhookDTO.getAgentId())
                .clientId(webhookDTO.getClientId())
                .provider(webhookDTO.getProvider())
                .externalCallId(webhookDTO.getCallId())
                .externalAgentId(webhookDTO.getAgentExternalId())
                .assistantId(webhookDTO.getAgentExternalId()) // Set assistantId to be the same as externalAgentId
                .startedAt(webhookDTO.getCallStartTime())
                .endedAt(webhookDTO.getCallEndTime())
                .audioUrl(webhookDTO.getRecordingUrl())
                .transcript(webhookDTO.getCallTranscript())
                .rawPayload(webhookDTO.getRawData())
                .status(status)
                .phoneNumber(webhookDTO.getPhoneNumber())
                .durationMinutes(durationMinutes)
                .build();
    }
}
