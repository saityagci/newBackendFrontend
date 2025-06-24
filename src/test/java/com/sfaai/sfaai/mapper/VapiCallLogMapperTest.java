package com.sfaai.sfaai.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.dto.VapiCallLogDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VapiCallLogMapperTest {

    private VapiWebhookMapper vapiWebhookMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private VapiAssistantRepository vapiAssistantRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        vapiWebhookMapper = new VapiWebhookMapper(objectMapper, clientRepository, agentRepository, vapiAssistantRepository);
    }

    @Test
    void testParseWebhookPayload() {
        // Create a sample payload
        VapiCallLogDTO callLog = new VapiCallLogDTO();
        Map<String, Object> properties = new HashMap<>();

        // Add call data
        Map<String, Object> call = new HashMap<>();
        call.put("id", "call_123456789");
        call.put("status", "completed");
        call.put("startTime", "1687452378");
        call.put("endTime", "1687452498");
        call.put("recordingUrl", "https://storage.vapi.ai/recordings/call_123456789.mp3");
        properties.put("call", call);

        // Add assistant data
        Map<String, Object> assistant = new HashMap<>();
        assistant.put("id", "asst_abcdef123456");
        assistant.put("name", "Customer Service Bot");
        properties.put("assistant", assistant);

        // Add transcript
        Map<String, Object> transcript = new HashMap<>();
        transcript.put("text", "User: Hello\nAssistant: Hi there, how can I help you today?");
        properties.put("transcript", transcript);

        // Add messages
        Map<String, Object> message1 = new HashMap<>();
        message1.put("role", "user");
        message1.put("content", "Hello");
        message1.put("timestamp", 1687452380);

        Map<String, Object> message2 = new HashMap<>();
        message2.put("role", "assistant");
        message2.put("content", "Hi there, how can I help you today?");
        message2.put("timestamp", 1687452385);

        properties.put("messages", Arrays.asList(message1, message2));

        callLog.setProperties(properties);

        // Parse the payload
        VapiCallLogDTO result = vapiWebhookMapper.parseWebhookPayload(callLog);

        // Verify extracted data
        assertEquals("call_123456789", result.getCallId());
        assertEquals("asst_abcdef123456", result.getAssistantId());
        assertEquals("completed", result.getStatus());
        assertEquals("https://storage.vapi.ai/recordings/call_123456789.mp3", result.getAudioUrl());
        assertEquals("User: Hello\nAssistant: Hi there, how can I help you today?", result.getTranscript());

        // Verify messages
        List<VapiCallLogDTO.MessageDTO> messages = result.getMessages();
        assertEquals(2, messages.size());

        // First message
        VapiCallLogDTO.MessageDTO userMessage = messages.get(0);
        assertEquals("user", userMessage.getRole());
        assertEquals("Hello", userMessage.getContent());

        // Second message
        VapiCallLogDTO.MessageDTO botMessage = messages.get(1);
        assertEquals("assistant", botMessage.getRole());
        assertEquals("Hi there, how can I help you today?", botMessage.getContent());
    }

    @Test
    void testToVoiceLogDTO() {
        // Create a sample call log
        VapiCallLogDTO callLog = VapiCallLogDTO.builder()
                .callId("call_123456789")
                .assistantId("asst_abcdef123456")
                .clientId(1L)
                .agentId(2L)
                .provider("vapi")
                .startTime(LocalDateTime.now().minusMinutes(5))
                .endTime(LocalDateTime.now())
                .audioUrl("https://storage.vapi.ai/recordings/call_123456789.mp3")
                .transcript("User: Hello\nAssistant: Hi there!")
                .build();

        // Add some messages
        VapiCallLogDTO.MessageDTO message1 = new VapiCallLogDTO.MessageDTO();
        message1.setRole("user");
        message1.setContent("Hello");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(4));

        VapiCallLogDTO.MessageDTO message2 = new VapiCallLogDTO.MessageDTO();
        message2.setRole("assistant");
        message2.setContent("Hi there!");
        message2.setTimestamp(LocalDateTime.now().minusMinutes(3));

        callLog.setMessages(Arrays.asList(message1, message2));

        // Convert to VoiceLogDTO
        VoiceLogDTO result = vapiWebhookMapper.toVoiceLogDTO(callLog);

        // Verify result
        assertEquals(1L, result.getClientId());
        assertEquals(2L, result.getAgentId());
        assertEquals("vapi", result.getProvider());
        assertEquals("call_123456789", result.getExternalCallId());
        assertEquals("asst_abcdef123456", result.getExternalAgentId());
        assertEquals(callLog.getStartTime(), result.getStartedAt());
        assertEquals(callLog.getEndTime(), result.getEndedAt());
        assertEquals("https://storage.vapi.ai/recordings/call_123456789.mp3", result.getAudioUrl());
        assertEquals("User: Hello\nAssistant: Hi there!", result.getTranscript());
    }
}
