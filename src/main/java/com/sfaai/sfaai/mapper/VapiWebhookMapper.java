package com.sfaai.sfaai.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiCallLogDTO;
import com.sfaai.sfaai.dto.VapiWebhookPayloadDTO;
import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper for Vapi webhook payloads
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VapiWebhookMapper {

    private final ObjectMapper objectMapper;

    /**
     * Parse a Vapi webhook payload into a structured DTO
     * @param payload The raw webhook payload
     * @return Parsed call log DTO
     */
    public VapiCallLogDTO parseWebhookPayload(VapiWebhookPayloadDTO payload) {
        if (payload == null) {
            return null;
        }

        try {
            VapiCallLogDTO.VapiCallLogDTOBuilder builder = VapiCallLogDTO.builder();

            // Store the entire raw payload as JSON string
            try {
                builder.rawPayload(objectMapper.writeValueAsString(payload.getProperties()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize raw payload", e);
                builder.rawPayload(payload.toString());
            }

            // Extract common fields with flexible path handling
            // Call ID
            builder.callId(extractFirstMatch(payload, new String[] {
                "call.id", "callId", "id", "call_id", "session_id", "sessionId"
            }));

            // Assistant ID
            builder.assistantId(extractFirstMatch(payload, new String[] {
                "assistant.id", "assistantId", "assistant_id", "botId", "bot_id"
            }));

            // Start time
            String startTimeStr = extractFirstMatch(payload, new String[] {
                "call.startTime", "startTime", "start_time", "started_at", "startedAt", "timestamp"
            });
            if (startTimeStr != null) {
                builder.startTime(parseTimestamp(startTimeStr));
            }

            // End time
            String endTimeStr = extractFirstMatch(payload, new String[] {
                "call.endTime", "endTime", "end_time", "ended_at", "endedAt"
            });
            if (endTimeStr != null) {
                builder.endTime(parseTimestamp(endTimeStr));
            }

            // Status
            builder.status(extractFirstMatch(payload, new String[] {
                "call.status", "status", "call_status", "state"
            }));

            // Audio URL
            builder.audioUrl(extractFirstMatch(payload, new String[] {
                "call.recordingUrl", "recordingUrl", "recording_url", "audioUrl", "audio_url", "media.url", "media_url"
            }));

            // Transcript
            builder.transcriptText(extractFirstMatch(payload, new String[] {
                "transcript.text", "transcriptText", "transcript_text", "transcript", "text"
            }));

            // Extract messages if present
            List<VapiCallLogDTO.MessageDTO> messages = extractMessages(payload);
            if (!messages.isEmpty()) {
                builder.messages(messages);

                // If no transcript was found but we have messages, build a transcript
                if (builder.build().getTranscriptText() == null) {
                    StringBuilder transcript = new StringBuilder();
                    for (VapiCallLogDTO.MessageDTO message : messages) {
                        transcript.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
                    }
                    builder.transcriptText(transcript.toString());
                }
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing Vapi webhook payload", e);
            // Return a minimal DTO with the raw payload
            return VapiCallLogDTO.builder()
                    .rawPayload(payload.toString())
                    .build();
        }
    }

    /**
     * Convert a VapiCallLogDTO to a VoiceLogCreateDTO
     * @param callLog The parsed call log
     * @param clientId The client ID
     * @param agentId The agent ID
     * @return VoiceLogCreateDTO for saving to database
     */
    public VoiceLogCreateDTO toVoiceLogCreateDTO(VapiCallLogDTO callLog, Long clientId, Long agentId) {
        if (callLog == null) {
            return null;
        }

                        // Convert messages list to JSON for storage
                        String conversationData = null;
                        if (callLog.getMessages() != null && !callLog.getMessages().isEmpty()) {
                            try {
                conversationData = objectMapper.writeValueAsString(callLog.getMessages());
                            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize conversation data", e);
                            }
                        }

                        return VoiceLogCreateDTO.builder()
                .clientId(clientId)
                .agentId(agentId)
                .provider("vapi")
                .externalCallId(callLog.getCallId())
                .externalAgentId(callLog.getAssistantId())
                .startedAt(callLog.getStartTime())
                .endedAt(callLog.getEndTime())
                .audioUrl(callLog.getAudioUrl())
                .transcript(callLog.getTranscriptText())
                .rawPayload(callLog.getRawPayload())
                .conversationData(conversationData)
                .build();
    }

    /**
     * Extract the first matching value from multiple possible paths
     * @param payload The webhook payload
     * @param paths Array of possible paths to check
     * @return The first matching value or null if none found
     */
    private String extractFirstMatch(VapiWebhookPayloadDTO payload, String[] paths) {
        for (String path : paths) {
            String value = payload.getStringValue(path);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Extract messages from the payload
     * @param payload The webhook payload
     * @return List of message DTOs
     */
    @SuppressWarnings("unchecked")
    private List<VapiCallLogDTO.MessageDTO> extractMessages(VapiWebhookPayloadDTO payload) {
        List<VapiCallLogDTO.MessageDTO> messages = new ArrayList<>();

        // Try several common paths for messages
        List<Object> messagesList = null;
        for (String path : new String[] {"messages", "conversation", "transcript.messages", "call.messages"}) {
            messagesList = payload.getListValue(path);
            if (messagesList != null && !messagesList.isEmpty()) {
                break;
            }
        }

        if (messagesList == null) {
            return messages;
        }

        // Process each message
        for (Object messageObj : messagesList) {
            if (!(messageObj instanceof Map)) {
                continue;
            }

            Map<String, Object> messageMap = (Map<String, Object>) messageObj;
            VapiCallLogDTO.MessageDTO.MessageDTOBuilder messageBuilder = VapiCallLogDTO.MessageDTO.builder();

            // Extract role
            Object roleObj = messageMap.get("role");
            if (roleObj != null) {
                messageBuilder.role(roleObj.toString());
            } else if (messageMap.containsKey("isFromUser") || messageMap.containsKey("is_from_user")) {
                boolean isFromUser = Boolean.TRUE.equals(messageMap.get("isFromUser")) || 
                                    Boolean.TRUE.equals(messageMap.get("is_from_user"));
                messageBuilder.role(isFromUser ? "user" : "assistant");
            }

            // Extract content
            Object contentObj = messageMap.get("content");
            if (contentObj == null) {
                contentObj = messageMap.get("text");
            }
            if (contentObj != null) {
                messageBuilder.content(contentObj.toString());
            }

            // Extract timestamp
            Object timestampObj = messageMap.get("timestamp");
            if (timestampObj == null) {
                timestampObj = messageMap.get("time");
            }
            if (timestampObj != null) {
                try {
                    messageBuilder.timestamp(parseTimestamp(timestampObj.toString()));
                } catch (Exception e) {
                    // Ignore timestamp parsing errors
                }
            }

            VapiCallLogDTO.MessageDTO message = messageBuilder.build();
            if (message.getContent() != null && message.getRole() != null) {
                messages.add(message);
            }
        }

        return messages;
    }

    /**
     * Parse a timestamp string into a LocalDateTime
     * @param timestamp Timestamp string (can be epoch seconds, milliseconds, or ISO string)
     * @return Parsed LocalDateTime or null if parsing fails
     */
    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }

        try {
            // Try parsing as epoch seconds or milliseconds
            if (timestamp.matches("\\d+")) {
                long value = Long.parseLong(timestamp);
                // If the value is in seconds (typical Unix timestamp)
                if (value < 20000000000L) { // Before year 2603
                    return LocalDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneId.systemDefault());
                } else {
                    // Assume milliseconds
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
                }
            }

            // Try parsing as ISO date time
            return LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            log.debug("Failed to parse timestamp: {}", timestamp);
            return null;
        }
    }
}
