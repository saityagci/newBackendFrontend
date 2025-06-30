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

                        // Ensure we have a valid audio URL - get directly from the DTO first
                        String audioUrl = webhookDTO.getRecordingUrl();

                        // If direct URL is missing, try to extract from raw data
                        if (audioUrl == null || audioUrl.isEmpty()) {
                            String rawData = webhookDTO.getRawData();
                            if (rawData != null && !rawData.isEmpty()) {
                                // Try to use our utility class first if it's available
                                try {
                                    Class.forName("com.sfaai.sfaai.util.AudioUrlExtractor");
                                    // If the class exists, use it
                                    audioUrl = com.sfaai.sfaai.util.AudioUrlExtractor.extractFromJson(rawData);
                                } catch (ClassNotFoundException e) {
                                    // AudioUrlExtractor not available, use inline extraction
                                    try {
                                        // First try detailed pattern with quotes
                                        java.util.regex.Pattern quotedPattern = java.util.regex.Pattern.compile(
                                            "\"((?:recordingUrl|recording_url|audioUrl|audio_url|mediaUrl|media_url))\"\s*:\s*\"(https?://[^\"]+)\"");
                                        java.util.regex.Matcher quotedMatcher = quotedPattern.matcher(rawData);

                                        if (quotedMatcher.find()) {
                                            audioUrl = quotedMatcher.group(2);
                                        } else {
                                            // Try more flexible pattern without strict quotes
                                            java.util.regex.Pattern flexPattern = java.util.regex.Pattern.compile(
                                                "(recordingUrl|recording_url|audioUrl|audio_url)\"?\s*:\s*\"?(https?://[^\"\s,}]+)");
                                            java.util.regex.Matcher flexMatcher = flexPattern.matcher(rawData);

                                            if (flexMatcher.find()) {
                                                audioUrl = flexMatcher.group(2);
                                            } else {
                                                // Last resort: look for any URL that appears to be an audio file
                                                java.util.regex.Pattern genericUrlPattern = java.util.regex.Pattern.compile(
                                                    "(https?://[^\"\s,}]+\\.(?:mp3|wav|m4a|ogg))");
                                                java.util.regex.Matcher genericMatcher = genericUrlPattern.matcher(rawData);

                                                if (genericMatcher.find()) {
                                                    audioUrl = genericMatcher.group(1);
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        // Ignore extraction errors and continue with null audioUrl
                                    }
                                }
                            }
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
                .audioUrl(audioUrl) // Use the extracted or original audioUrl
                .transcript(webhookDTO.getCallTranscript())
                .rawPayload(webhookDTO.getRawData())
                .status(status)
                .phoneNumber(webhookDTO.getPhoneNumber())
                .durationMinutes(durationMinutes)
                .build();
    }
}
