package com.sfaai.sfaai.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiCallLogDTO;
import com.sfaai.sfaai.dto.VapiWebhookPayloadDTO;
import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public VapiCallLogDTO parseWebhookPayload(VapiWebhookPayloadDTO payload) throws JsonProcessingException {
        if (payload == null) {
            log.warn("Received null webhook payload");
            return null;
        }

        try {
            // ---- Stage 1: Parse root and message if present ----
            Map<String, Object> rootMap = objectMapper.convertValue(payload, Map.class);

            Map<String, Object> messageMap = null;
            if (rootMap.containsKey("message") && rootMap.get("message") instanceof Map) {
                messageMap = (Map<String, Object>) rootMap.get("message");
                log.debug("Found nested 'message' node, using it as a secondary field source");
            }

            // Helper function to get value from both maps (root, then message)
            Map<String, Object> finalMessageMap = messageMap;
            java.util.function.Function<String, Object> getField = key -> {
                Object val = rootMap.get(key);
                if (val == null && finalMessageMap != null) val = finalMessageMap.get(key);
                return val;
            };

            VapiCallLogDTO.VapiCallLogDTOBuilder builder = VapiCallLogDTO.builder();

            // ---- Minimal raw payload for debug ----
            try {
                Map<String, Object> minimalPayload = new HashMap<>();
                Map<String, Object> callInfo = (Map<String, Object>) getField.apply("call");
                Map<String, Object> assistantInfo = (Map<String, Object>) getField.apply("assistant");

                if (callInfo != null) {
                    Map<String, Object> minimalCall = new HashMap<>();
                    minimalCall.put("id", callInfo.get("id"));
                    minimalCall.put("status", callInfo.get("status"));
                    minimalCall.put("startTime", callInfo.get("startTime"));
                    minimalCall.put("endTime", callInfo.get("endTime"));
                    minimalCall.put("recordingUrl", callInfo.get("recordingUrl"));
                    minimalPayload.put("call", minimalCall);
                }
                if (assistantInfo != null) {
                    Map<String, Object> minimalAssistant = new HashMap<>();
                    minimalAssistant.put("id", assistantInfo.get("id"));
                    minimalAssistant.put("name", assistantInfo.get("name"));
                    minimalPayload.put("assistant", minimalAssistant);
                }
                builder.rawPayload(objectMapper.writeValueAsString(minimalPayload));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize minimal raw payload", e);
                builder.rawPayload("{}");
            }

            // ---- assistantId ----
            String assistantId =
                    (String) rootMap.get("assistant_id");
            if ((assistantId == null || assistantId.isEmpty()) && messageMap != null) {
                Map<String, Object> assistant = (Map<String, Object>) messageMap.get("assistant");
                if (assistant != null) assistantId = (String) assistant.get("id");
            }
            if (assistantId == null || assistantId.isEmpty()) {
                log.error("FINAL ASSISTANT ID FAIL. Payload as map: {}", objectMapper.writeValueAsString(rootMap));
                throw new IllegalArgumentException("assistantId is required but missing in webhook payload");
            }
            log.info("SUCCESS: Extracted assistantId: {}", assistantId);
            builder.assistantId(assistantId);

            // ---- callId ----
            String callId = (String) rootMap.get("call_id");
            if ((callId == null || callId.isEmpty()) && messageMap != null) {
                Map<String, Object> call = (Map<String, Object>) messageMap.get("call");
                if (call != null) callId = (String) call.get("id");
            }
            builder.callId(callId);

            // ---- phone number ----
            String phoneNumber = (String) rootMap.get("caller_phone_number");
            if ((phoneNumber == null || phoneNumber.isEmpty()) && messageMap != null) {
                Map<String, Object> call = (Map<String, Object>) messageMap.get("call");
                if (call != null) {
                    Map<String, Object> customer = (Map<String, Object>) call.get("customer");
                    if (customer != null) phoneNumber = (String) customer.get("number");
                }
            }
            // Try more paths for phone number
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                phoneNumber = (String) rootMap.get("callerPhoneNumber");
            }
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                phoneNumber = (String) getField.apply("phone_number");
            }
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                phoneNumber = (String) getField.apply("customer_number");
            }
            builder.phoneNumber(phoneNumber);

            // ---- summary ----
            String summary = (String) rootMap.get("summary");
            if ((summary == null || summary.isEmpty()) && messageMap != null) {
                Map<String, Object> analysis = (Map<String, Object>) messageMap.get("analysis");
                if (analysis != null) summary = (String) analysis.get("summary");
            }

            // ---- audioUrl ----
            String audioUrl = (String) rootMap.get("audio_url");
            if ((audioUrl == null || audioUrl.isEmpty()) && messageMap != null) {
                Map<String, Object> artifact = (Map<String, Object>) messageMap.get("artifact");
                if (artifact != null && artifact.get("recording_url") != null)
                    audioUrl = artifact.get("recording_url").toString();
            }
            // Try alternative fields for audio URL
            if (audioUrl == null || audioUrl.isEmpty()) {
                audioUrl = (String) rootMap.get("recordingUrl");
            }
            // Try the call object for recording URL
            Map<String, Object> callInfo = (Map<String, Object>) getField.apply("call");
            if (audioUrl == null || audioUrl.isEmpty() && callInfo != null) {
                audioUrl = (String) callInfo.get("recordingUrl");
            }
            // Try artifact at root level
            if (audioUrl == null || audioUrl.isEmpty()) {
                Map<String, Object> artifact = (Map<String, Object>) rootMap.get("artifact");
                if (artifact != null) {
                    if (artifact.get("recording_url") != null) {
                        audioUrl = artifact.get("recording_url").toString();
                    } else if (artifact.get("recordingUrl") != null) {
                        audioUrl = artifact.get("recordingUrl").toString();
                    } else if (artifact.get("audio_url") != null) {
                        audioUrl = artifact.get("audio_url").toString();
                    } else if (artifact.get("audioUrl") != null) {
                        audioUrl = artifact.get("audioUrl").toString();
                    }
                }
            }
            // Try recording directly in root
            if (audioUrl == null || audioUrl.isEmpty()) {
                if (rootMap.get("recording") instanceof Map) {
                    Map<String, Object> recording = (Map<String, Object>) rootMap.get("recording");
                    if (recording.get("url") != null) {
                        audioUrl = recording.get("url").toString();
                    }
                }
            }

            log.debug("Extracted audioUrl: {}", audioUrl != null ? audioUrl : "<none>");
            builder.audioUrl(audioUrl);

            // ---- durationMinutes ----
            Float durationMinutes = null;
            // Try direct field first
            Object durationObj = getField.apply("durationMinutes");
            if (durationObj instanceof Number) {
                durationMinutes = ((Number) durationObj).floatValue();
            } else if (durationObj instanceof String) {
                try {
                    durationMinutes = Float.parseFloat((String) durationObj);
                } catch (NumberFormatException e) {
                    log.debug("Could not parse durationMinutes as float: {}", durationObj);
                }
            }

            // Try alternative fields if not found
            if (durationMinutes == null) {
                Object durationSecondsObj = getField.apply("durationSeconds");
                if (durationSecondsObj instanceof Number) {
                    durationMinutes = ((Number) durationSecondsObj).floatValue() / 60.0f;
                } else if (durationSecondsObj instanceof String) {
                    try {
                        durationMinutes = Float.parseFloat((String) durationSecondsObj) / 60.0f;
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse durationSeconds as float: {}", durationSecondsObj);
                    }
                }
            }

            // Try duration field (which could be in seconds)
            if (durationMinutes == null) {
                Object duration = getField.apply("duration");
                if (duration instanceof Number) {
                    // Assume it's in seconds if greater than 500 (no call is 500 minutes)
                    double value = ((Number) duration).doubleValue();
                    if (value > 500) {
                        durationMinutes = (float) (value / 60.0);
                    } else {
                        durationMinutes = (float) value;
                    }
                } else if (duration instanceof String) {
                    try {
                        double value = Double.parseDouble((String) duration);
                        if (value > 500) {
                            durationMinutes = (float) (value / 60.0);
                        } else {
                            durationMinutes = (float) value;
                        }
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse duration as float: {}", duration);
                    }
                }
            }

            // Try to extract from artifact
            if (durationMinutes == null) {
                Map<String, Object> artifact = (Map<String, Object>) getField.apply("artifact");
                if (artifact != null) {
                    Object artifactDuration = artifact.get("durationMinutes");
                    if (artifactDuration == null) artifactDuration = artifact.get("duration_minutes");
                    if (artifactDuration == null) artifactDuration = artifact.get("duration");

                    if (artifactDuration instanceof Number) {
                        double value = ((Number) artifactDuration).doubleValue();
                        // If it's larger than 500, assume it's in seconds
                        if (value > 500) {
                            durationMinutes = (float) (value / 60.0);
                        } else {
                            durationMinutes = (float) value;
                        }
                    } else if (artifactDuration instanceof String) {
                        try {
                            double value = Double.parseDouble((String) artifactDuration);
                            if (value > 500) {
                                durationMinutes = (float) (value / 60.0);
                            } else {
                                durationMinutes = (float) value;
                            }
                        } catch (NumberFormatException e) {
                            log.debug("Could not parse artifact duration as float: {}", artifactDuration);
                        }
                    }
                }
            }

            // ---- startTime/endTime ----
            LocalDateTime startTime = null, endTime = null;

            // Check call object first
            if (callInfo != null) {
                if (callInfo.get("startTime") != null) {
                    startTime = parseTimestamp(callInfo.get("startTime").toString());
                } else if (callInfo.get("start_time") != null) {
                    startTime = parseTimestamp(callInfo.get("start_time").toString());
                }

                if (callInfo.get("endTime") != null) {
                    endTime = parseTimestamp(callInfo.get("endTime").toString());
                } else if (callInfo.get("end_time") != null) {
                    endTime = parseTimestamp(callInfo.get("end_time").toString());
                }
            }

            // Try root level fields if not found in call object
            if (startTime == null) {
                Object startTimeObj = getField.apply("startTime");
                if (startTimeObj == null) startTimeObj = getField.apply("start_time");
                if (startTimeObj == null) startTimeObj = getField.apply("startedAt");
                if (startTimeObj == null) startTimeObj = getField.apply("started_at");
                if (startTimeObj != null) {
                    startTime = parseTimestamp(startTimeObj.toString());
                }
            }

            if (endTime == null) {
                Object endTimeObj = getField.apply("endTime");
                if (endTimeObj == null) endTimeObj = getField.apply("end_time");
                if (endTimeObj == null) endTimeObj = getField.apply("endedAt");
                if (endTimeObj == null) endTimeObj = getField.apply("ended_at");
                if (endTimeObj != null) {
                    endTime = parseTimestamp(endTimeObj.toString());
                }
            }

            // Check artifact if we still don't have times
            if (startTime == null || endTime == null) {
                Map<String, Object> artifact = (Map<String, Object>) getField.apply("artifact");
                if (artifact != null) {
                    if (startTime == null) {
                        Object artStartTime = artifact.get("startTime");
                        if (artStartTime == null) artStartTime = artifact.get("start_time");
                        if (artStartTime != null) {
                            startTime = parseTimestamp(artStartTime.toString());
                        }
                    }

                    if (endTime == null) {
                        Object artEndTime = artifact.get("endTime");
                        if (artEndTime == null) artEndTime = artifact.get("end_time");
                        if (artEndTime != null) {
                            endTime = parseTimestamp(artEndTime.toString());
                        }
                    }
                }
            }

            log.debug("Extracted startTime: {}, endTime: {}", startTime, endTime);
            builder.startTime(startTime);
            builder.endTime(endTime);

            // If durationMinutes is still not found and we have start/end times, calculate it
            if (durationMinutes == null && startTime != null && endTime != null) {
                long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
                durationMinutes = seconds / 60.0f;
            }

            // Set duration field
            builder.duration(durationMinutes != null ? durationMinutes.intValue() : null);
            // Also set durationMinutes for direct mapping
            builder.durationMinutes(durationMinutes);

            // ---- transcript ----
            String transcript = (String) rootMap.get("transcript");
            if ((transcript == null || transcript.isEmpty()) && messageMap != null) {
                Map<String, Object> artifact = (Map<String, Object>) messageMap.get("artifact");
                if (artifact != null && artifact.get("transcript") != null)
                    transcript = artifact.get("transcript").toString();
            }
            // Add summary to transcript if present
            if (summary != null && !summary.isEmpty()) {
                if (transcript == null || transcript.isEmpty()) transcript = "Summary: " + summary;
                else transcript += "\n\nSummary: " + summary;
            }
            builder.transcriptText(transcript);

            // ---- status ----
            String status = null;
            if (callInfo != null && callInfo.get("status") != null)
                status = callInfo.get("status").toString();
            builder.status(status);

            // ---- messages ----
            List<VapiCallLogDTO.MessageDTO> messages = new ArrayList<>();
            Map<String, Object> artifactMap = (Map<String, Object>) getField.apply("artifact");
            if (artifactMap != null && artifactMap.get("messages") instanceof List) {
                List<?> msgList = (List<?>) artifactMap.get("messages");
                for (Object obj : msgList) {
                    if (!(obj instanceof Map)) continue;
                    Map<?, ?> msg = (Map<?, ?>) obj;
                    String role = msg.get("role") != null ? msg.get("role").toString() : null;
                    String content = msg.get("message") != null ? msg.get("message").toString() :
                            (msg.get("content") != null ? msg.get("content").toString() : null);
                    LocalDateTime ts = null;
                    if (msg.get("time") != null) ts = parseTimestamp(msg.get("time").toString());
                    messages.add(VapiCallLogDTO.MessageDTO.builder()
                            .role(role)
                            .content(content)
                            .timestamp(ts)
                            .build());
                }
            }
            builder.messages(messages);

            // ---- fallback transcript from messages ----
            if ((transcript == null || transcript.isEmpty()) && !messages.isEmpty()) {
                StringBuilder transcriptBuilder = new StringBuilder();
                for (VapiCallLogDTO.MessageDTO m : messages) {
                    if (m.getRole() != null && m.getContent() != null)
                        transcriptBuilder.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
                }
                builder.transcriptText(transcriptBuilder.toString().trim());
            }

            // --- Final ---
            VapiCallLogDTO result = builder.build();
            log.debug("Parsed VapiCallLogDTO: callId={}, assistantId={}, status={}, audioUrl={}, messagesCount={}",
                    result.getCallId(), result.getAssistantId(), result.getStatus(),
                    result.getAudioUrl() != null ? "present" : "missing",
                    result.getMessages() != null ? result.getMessages().size() : 0);

            return result;
        } catch (Exception e) {
            log.error("Error parsing Vapi webhook payload", e);
            throw e;
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
            log.warn("Cannot convert null callLog to VoiceLogCreateDTO");
            return null;
        }

        if (clientId == null || agentId == null) {
            log.warn("Cannot create VoiceLogCreateDTO with null clientId or agentId");
            return null;
        }

        // Convert messages list to JSON for storage
        String conversationData = null;
        if (callLog.getMessages() != null && !callLog.getMessages().isEmpty()) {
            try {
                // Filter out any messages with null content or role before serializing
                List<VapiCallLogDTO.MessageDTO> validMessages = callLog.getMessages().stream()
                    .filter(msg -> msg.getContent() != null && msg.getRole() != null)
                    .collect(java.util.stream.Collectors.toList());

                if (!validMessages.isEmpty()) {
                    conversationData = objectMapper.writeValueAsString(validMessages);
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize conversation data", e);
                // Create a simplified fallback version to ensure we store something
                try {
                    StringBuilder fallback = new StringBuilder("[");
                    for (int i = 0; i < callLog.getMessages().size(); i++) {
                        VapiCallLogDTO.MessageDTO msg = callLog.getMessages().get(i);
                        if (msg.getRole() != null && msg.getContent() != null) {
                            if (i > 0) fallback.append(",");
                            fallback.append("{\"role\":\"")
                                   .append(msg.getRole())
                                   .append("\",\"content\":\"")
                                   .append(msg.getContent().replace("\"", "\\\""))
                                   .append("\"}");
                        }
                    }
                    fallback.append("]");
                    conversationData = fallback.toString();
                } catch (Exception ex) {
                    log.error("Failed to create fallback conversation data", ex);
                }
            }
        }

        // Ensure we have a valid status for the VoiceLog entity
        VoiceLog.Status logStatus = VoiceLog.Status.COMPLETED; // Default status
        if (callLog.getStatus() != null) {
            String status = callLog.getStatus().toUpperCase();
            try {
                // Try to map Vapi status to our status enum
                if (status.contains("FAIL") || status.contains("ERROR")) {
                    logStatus = VoiceLog.Status.FAILED;
                } else if (status.contains("CANCEL")) {
                    logStatus = VoiceLog.Status.CANCELLED;
                } else if (status.contains("RING")) {
                    logStatus = VoiceLog.Status.RINGING;
                } else if (status.contains("PROGRESS") || status.contains("ACTIVE") || status.contains("ONGOING")) {
                    logStatus = VoiceLog.Status.IN_PROGRESS;
                } else if (status.contains("INIT")) {
                    logStatus = VoiceLog.Status.INITIATED;
                }
                // Default is COMPLETED
            } catch (Exception e) {
                log.warn("Failed to map status '{}' to VoiceLog.Status enum, using default COMPLETED", status);
            }
        }

        // Build the voice log create DTO with null checks

        // Use the explicit durationMinutes if available, otherwise fall back to calculated from duration
        Float finalDurationMinutes = callLog.getDurationMinutes();
        if (finalDurationMinutes == null && callLog.getDuration() != null) {
            finalDurationMinutes = callLog.getDuration().floatValue() / 60.0f;
        }

        // If we have startTime and endTime but no duration, calculate it
        if (finalDurationMinutes == null && callLog.getStartTime() != null && callLog.getEndTime() != null) {
            long seconds = java.time.Duration.between(callLog.getStartTime(), callLog.getEndTime()).getSeconds();
            finalDurationMinutes = seconds / 60.0f;
        }

        log.debug("Mapping to VoiceLogCreateDTO - startedAt: {}, endedAt: {}, audioUrl: {}, durationMinutes: {}",
                callLog.getStartTime(), callLog.getEndTime(), 
                callLog.getAudioUrl() != null ? "present" : "null", 
                finalDurationMinutes);

        return VoiceLogCreateDTO.builder()
                .clientId(clientId)
                .agentId(agentId)
                .provider("vapi")
                .externalCallId(callLog.getCallId())
                .externalAgentId(callLog.getAssistantId())
                .startedAt(callLog.getStartTime())
                .assistantId(callLog.getAssistantId())
                .endedAt(callLog.getEndTime())
                .audioUrl(callLog.getAudioUrl())
                .transcript(callLog.getTranscriptText())
                // Only store minimal raw payload to avoid DB bloat
                .rawPayload(trimRawPayload(callLog.getRawPayload()))
                .conversationData(conversationData)
                .status(logStatus)
                .phoneNumber(callLog.getPhoneNumber())
                .durationMinutes(finalDurationMinutes)
                .build();
    }

    /**
     * Trim the raw payload to a reasonable size to avoid DB bloat
     * @param rawPayload The raw payload string
     * @return Trimmed raw payload
     */
    private String trimRawPayload(String rawPayload) {
        if (rawPayload == null) {
            return null;
        }

        // Limit payload size to avoid database issues
        final int MAX_PAYLOAD_SIZE = 8192; // 8KB is enough for debugging purposes

        if (rawPayload.length() > MAX_PAYLOAD_SIZE) {
            return rawPayload.substring(0, MAX_PAYLOAD_SIZE - 32) + "... [truncated, full size: " + rawPayload.length() + " bytes]";
        }

        return rawPayload;
    }

    /**
     * Extract the first matching value from multiple possible paths
     * @param payload The webhook payload
     * @param paths Array of possible paths to check
     * @return The first matching value or null if none found
     */
    private String extractFirstMatch(VapiWebhookPayloadDTO payload, String[] paths) {
        if (payload == null || paths == null || paths.length == 0) {
            return null;
        }

        for (String path : paths) {
            if (path == null || path.isEmpty()) {
                continue;
            }

            String value = payload.getStringValue(path);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * Extract messages from the Vapi webhook payload
     * @param payload The webhook payload
     * @return List of message DTOs (never null, but may be empty)
     */
    @SuppressWarnings("unchecked")
    private List<VapiCallLogDTO.MessageDTO> extractMessages(VapiWebhookPayloadDTO payload) {
        if (payload == null) {
            return Collections.emptyList();
        }

        List<VapiCallLogDTO.MessageDTO> messages = new ArrayList<>();

        // Try several common paths for messages based on Vapi's documentation
        List<Object> messagesList = null;
        String[] messagePaths = {
            "messages",               // Standard Vapi path
            "conversation",           // Alternative path
            "transcript.messages",    // Nested in transcript object
            "call.messages",         // Nested in call object
            "turns"                  // Some voice APIs use this term
        };

        for (String path : messagePaths) {
            messagesList = payload.getListValue(path);
            if (messagesList != null && !messagesList.isEmpty()) {
                log.debug("Found messages at path: {}", path);
                break;
            }
        }

        if (messagesList == null) {
            log.debug("No messages found in payload");
            return messages;
        }

        // Process each message
        for (Object messageObj : messagesList) {
            if (!(messageObj instanceof Map)) {
                continue;
            }

            Map<String, Object> messageMap = (Map<String, Object>) messageObj;
            VapiCallLogDTO.MessageDTO.MessageDTOBuilder messageBuilder = VapiCallLogDTO.MessageDTO.builder();

            // Extract role with multiple fallbacks
            String role = null;
            // Direct role field
            Object roleObj = messageMap.get("role");
            if (roleObj != null) {
                role = roleObj.toString();
            } 
            // Boolean flag indicating user or assistant
            else if (messageMap.containsKey("isFromUser") || messageMap.containsKey("is_from_user")) {
                boolean isFromUser = Boolean.TRUE.equals(messageMap.get("isFromUser")) || 
                                    Boolean.TRUE.equals(messageMap.get("is_from_user"));
                role = isFromUser ? "user" : "assistant";
            }
            // Speaker field
            else if (messageMap.containsKey("speaker")) {
                Object speaker = messageMap.get("speaker");
                if (speaker != null) {
                    String speakerStr = speaker.toString().toLowerCase();
                    if (speakerStr.contains("user") || speakerStr.contains("caller") || speakerStr.contains("customer")) {
                        role = "user";
                    } else if (speakerStr.contains("assistant") || speakerStr.contains("agent") || speakerStr.contains("bot")) {
                        role = "assistant";
                    } else {
                        role = speakerStr; // Use the actual value as fallback
                    }
                }
            }

            // Extract message type
            String messageType = null;
            Object typeObj = messageMap.get("type");
            if (typeObj != null) {
                messageType = typeObj.toString();
            }
            messageBuilder.messageType(messageType);

            // Extract confidence score
            Double confidence = null;
            Object confidenceObj = messageMap.get("confidence");
            if (confidenceObj instanceof Number) {
                confidence = ((Number) confidenceObj).doubleValue();
            } else if (confidenceObj instanceof String) {
                try {
                    confidence = Double.parseDouble(confidenceObj.toString());
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
            messageBuilder.confidence(confidence);

            // Extract additional metadata
            Map<String, Object> metadata = new HashMap<>();
            for (String key : messageMap.keySet()) {
                // Skip standard fields we've already extracted
                if (!Arrays.asList("role", "content", "text", "timestamp", "time", "speaker", 
                        "isFromUser", "is_from_user", "type", "confidence").contains(key)) {
                    Object value = messageMap.get(key);
                    if (value != null) {
                        metadata.put(key, value);
                    }
                }
            }
            if (!metadata.isEmpty()) {
                messageBuilder.metadata(metadata);
            }

            messageBuilder.role(role);

            // Extract content with fallbacks
            String content = null;
            String[] contentFields = {"content", "text", "message", "value", "transcript"};
            for (String field : contentFields) {
                Object contentObj = messageMap.get(field);
                if (contentObj != null) {
                    content = contentObj.toString();
                    break;
                }
            }
            messageBuilder.content(content);

            // Extract timestamp with fallbacks
            String[] timeFields = {"timestamp", "time", "created_at", "createdAt", "datetime"};
            LocalDateTime timestamp = null;
            for (String field : timeFields) {
                Object timeObj = messageMap.get(field);
                if (timeObj != null) {
                    try {
                        timestamp = parseTimestamp(timeObj.toString());
                        break;
                    } catch (Exception e) {
                        // Continue to next field on parsing error
                    }
                }
            }
            messageBuilder.timestamp(timestamp);

            // Only add valid messages
            VapiCallLogDTO.MessageDTO message = messageBuilder.build();
            if (message.getContent() != null && !message.getContent().trim().isEmpty() && 
                message.getRole() != null && !message.getRole().trim().isEmpty()) {
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
            try {
                return LocalDateTime.parse(timestamp);
            } catch (Exception e) {
                // Try with java.time.format.DateTimeFormatter
                java.time.format.DateTimeFormatter[] formatters = {
                    java.time.format.DateTimeFormatter.ISO_DATE_TIME,
                    java.time.format.DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()),
                    java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                };

                for (java.time.format.DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(timestamp, formatter);
                    } catch (Exception ex) {
                        // Try next formatter
                    }
                }

                // None of the formatters worked, throw to be caught by outer catch
                throw new IllegalArgumentException("Cannot parse timestamp: " + timestamp);
            }
        } catch (Exception e) {
            log.debug("Failed to parse timestamp: {} ({})", timestamp, e.getMessage());
            return null;
        }
    }
}