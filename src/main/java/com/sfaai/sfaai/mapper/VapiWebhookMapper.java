package com.sfaai.sfaai.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiCallLogDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import lombok.Getter;
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
 * Mapper for Vapi webhook payloads and voice logs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VapiWebhookMapper {

    @Getter // Make getter accessible for controllers
    private final ObjectMapper objectMapper;
    private final ClientRepository clientRepository;
    private final AgentRepository agentRepository;
    private final VapiAssistantRepository vapiAssistantRepository;

    /**
     * Parse webhook properties into the same DTO
     * @param callLog The call log to populate with parsed data
     * @return The populated call log DTO
     */
    public VapiCallLogDTO parseWebhookPayload(VapiCallLogDTO callLog) {
        if (callLog == null) {
            return null;
        }

        try {
            // Store the entire raw payload as JSON string if not already set
            if (callLog.getRawPayload() == null && !callLog.getProperties().isEmpty()) {
                try {
                    callLog.setRawPayload(objectMapper.writeValueAsString(callLog.getProperties()));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize raw payload", e);
                    callLog.setRawPayload(callLog.getProperties().toString());
                }
            }

            // Extract common fields with flexible path handling if not already set directly
            // Call ID
            if (callLog.getCallId() == null) {
                callLog.setCallId(extractFirstMatch(callLog, new String[] {
                    "call.id", "callId", "id", "call_id", "session_id", "sessionId"
                }));
            }

            // Assistant ID
            if (callLog.getAssistantId() == null) {
                callLog.setAssistantId(extractFirstMatch(callLog, new String[] {
                    "assistant.id", "assistantId", "assistant_id", "botId", "bot_id"
                }));
            }

            // Start time
            if (callLog.getStartTime() == null && callLog.getStartedAt() == null) {
                String startTimeStr = extractFirstMatch(callLog, new String[] {
                    "call.startTime", "startTime", "start_time", "started_at", "startedAt", "timestamp"
                });
                if (startTimeStr != null) {
                    LocalDateTime time = parseTimestamp(startTimeStr);
                    callLog.setStartTime(time);
                    callLog.setStartedAt(time);
                }
            }

            // End time
            if (callLog.getEndTime() == null && callLog.getEndedAt() == null) {
                String endTimeStr = extractFirstMatch(callLog, new String[] {
                    "call.endTime", "endTime", "end_time", "ended_at", "endedAt"
                });
                if (endTimeStr != null) {
                    LocalDateTime time = parseTimestamp(endTimeStr);
                    callLog.setEndTime(time);
                    callLog.setEndedAt(time);
                }
            }

            // Status
            if (callLog.getStatus() == null) {
                callLog.setStatus(extractFirstMatch(callLog, new String[] {
                    "call.status", "status", "call_status", "state"
                }));
            }

            // Audio URL
            if (callLog.getAudioUrl() == null) {
                callLog.setAudioUrl(extractFirstMatch(callLog, new String[] {
                    "call.recordingUrl", "recordingUrl", "recording_url", "audioUrl", "audio_url", "media.url", "media_url"
                }));
            }

            // Transcript - try to get from properties if not already set
            if (callLog.getTranscript() == null && callLog.getTranscriptText() == null) {
                String transcript = extractFirstMatch(callLog, new String[] {
                    "transcript.text", "transcriptText", "transcript_text", "transcript", "text"
                });
                if (transcript != null) {
                    callLog.setTranscript(transcript);
                    callLog.setTranscriptText(transcript);
                }
            }

            // Extract messages if present and not already populated
            if ((callLog.getMessages() == null || callLog.getMessages().isEmpty()) && !callLog.getProperties().isEmpty()) {
                List<VapiCallLogDTO.MessageDTO> messages = extractMessages(callLog);
                if (!messages.isEmpty()) {
                    callLog.setMessages(messages);

                    // If no transcript was found, but we have messages, build a transcript
                    if (callLog.getTranscript() == null && callLog.getTranscriptText() == null) {
                        StringBuilder transcript = new StringBuilder();
                        for (VapiCallLogDTO.MessageDTO message : messages) {
                            transcript.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
                        }
                        callLog.setTranscript(transcript.toString());
                        callLog.setTranscriptText(transcript.toString());
                    }
                }
            }

            return callLog;

        } catch (Exception e) {
            log.error("Error parsing Vapi webhook payload", e);
            // If there was an error, at least keep the raw payload
            if (callLog.getRawPayload() == null && !callLog.getProperties().isEmpty()) {
                callLog.setRawPayload(callLog.getProperties().toString());
            }
            return callLog;
        }
    }

    /**
     * Convert VapiCallLogDTO to VoiceLogDTO for saving to database
     * @param callLog The call log to convert
     * @return Voice log DTO for persistence
     */
    public VoiceLogDTO toVoiceLogDTO(VapiCallLogDTO callLog) {
        if (callLog == null) {
            return null;
        }

        VoiceLogDTO.VoiceLogDTOBuilder builder = VoiceLogDTO.builder()
                .clientId(callLog.getClientId())
                .agentId(callLog.getAgentId())
                .provider(callLog.getProvider())
                .externalCallId(callLog.getExternalCallId() != null ? callLog.getExternalCallId() : callLog.getCallId())
                .externalAgentId(callLog.getExternalAgentId() != null ? callLog.getExternalAgentId() : callLog.getAssistantId())
                .startedAt(callLog.getStartedAt() != null ? callLog.getStartedAt() : callLog.getStartTime())
                .endedAt(callLog.getEndedAt() != null ? callLog.getEndedAt() : callLog.getEndTime())
                .audioUrl(callLog.getAudioUrl())
                .transcript(callLog.getTranscript() != null ? callLog.getTranscript() : callLog.getTranscriptText())
                .rawPayload(callLog.getRawPayload())
                .conversationData(callLog.getConversationData());

        if (callLog.getId() != null) {
            builder.id(callLog.getId());
        }

        return builder.build();
    }

    /**
     * Create a VoiceLog entity from a VapiCallLogDTO
     * @param callLog The call log DTO
     * @return VoiceLog entity ready for persistence
     */
    public VoiceLog createVoiceLogEntity(VapiCallLogDTO callLog) {
        if (callLog == null) {
            return null;
        }

        VoiceLog.VoiceLogBuilder builder = VoiceLog.builder()
                .externalCallId(callLog.getExternalCallId() != null ? callLog.getExternalCallId() : callLog.getCallId())
                .provider(callLog.getProvider())
                .externalAgentId(callLog.getExternalAgentId() != null ? callLog.getExternalAgentId() : callLog.getAssistantId())
                .startedAt(callLog.getStartedAt() != null ? callLog.getStartedAt() : callLog.getStartTime())
                .endedAt(callLog.getEndedAt() != null ? callLog.getEndedAt() : callLog.getEndTime())
                .audioUrl(callLog.getAudioUrl())
                .transcript(callLog.getTranscript() != null ? callLog.getTranscript() : callLog.getTranscriptText())
                .rawPayload(callLog.getRawPayload())
                .conversationData(callLog.getConversationData())
                .status(VoiceLog.Status.INITIATED); // Default status

        if (callLog.getId() != null) {
            builder.id(callLog.getId());
        }

        // Set up relationships
        if (callLog.getClientId() != null) {
            Client client = clientRepository.findById(callLog.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found with id: " + callLog.getClientId()));
            builder.client(client);
        }

        if (callLog.getAgentId() != null) {
            Agent agent = agentRepository.findById(callLog.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + callLog.getAgentId()));
            builder.agent(agent);
        }

        if (callLog.getAssistantId() != null) {
            VapiAssistant assistant = vapiAssistantRepository.findById(callLog.getAssistantId())
                    .orElseThrow(() -> new RuntimeException("Assistant not found with id: " + callLog.getAssistantId()));
            builder.vapiAssistant(assistant);
        }

        return builder.build();
    }

    /**
     * Extract the first matching value from multiple possible paths
     * @param callLog The call log with properties
     * @param paths Array of possible paths to check
     * @return The first matching value or null if none found
     */
    private String extractFirstMatch(VapiCallLogDTO callLog, String[] paths) {
        for (String path : paths) {
            String value = callLog.getStringValue(path);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Extract messages from the payload
     * @param callLog The call log with properties
     * @return List of message DTOs
     */
    @SuppressWarnings("unchecked")
    private List<VapiCallLogDTO.MessageDTO> extractMessages(VapiCallLogDTO callLog) {
        List<VapiCallLogDTO.MessageDTO> messages = new ArrayList<>();

        // Try several common paths for messages
        List<Object> messagesList = null;
        for (String path : new String[] {"messages", "conversation", "transcript.messages", "call.messages"}) {
            messagesList = callLog.getListValue(path);
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
