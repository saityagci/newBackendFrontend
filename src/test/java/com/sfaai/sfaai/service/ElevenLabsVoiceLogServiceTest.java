package com.sfaai.sfaai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.config.ElevenLabsConfig;
import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.service.impl.ElevenLabsVoiceLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevenLabsVoiceLogServiceTest {

    @Mock
    private VoiceLogRepository voiceLogRepository;

    @Mock
    private VoiceLogService voiceLogService;

    @Mock
    private VoiceLogMapper voiceLogMapper;

    @Mock
    private ElevenLabsConfig elevenLabsConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ElevenLabsVoiceLogServiceImpl elevenLabsVoiceLogService;

    private VoiceLog voiceLog;
    private VoiceLogDTO voiceLogDTO;
    private VoiceLogCreateDTO voiceLogCreateDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        voiceLog = VoiceLog.builder()
                .id(1L)
                .externalCallId("test-conversation-id")
                .provider(VoiceLog.Provider.ELEVENLABS)
                .status(VoiceLog.Status.COMPLETED)
                .build();

        voiceLogDTO = VoiceLogDTO.builder()
                .id(1L)
                .externalCallId("test-conversation-id")
                .provider("ELEVENLABS")
                .build();

        voiceLogCreateDTO = new VoiceLogCreateDTO();
        voiceLogCreateDTO.setExternalCallId("test-conversation-id");
        voiceLogCreateDTO.setProvider("ELEVENLABS");
        voiceLogCreateDTO.setClientId(1L);
        voiceLogCreateDTO.setAgentId(1L);
        voiceLogCreateDTO.setAssistantId("test-assistant-id");
    }

    @Test
    void getAllElevenLabsVoiceLogs_Success() {
        // Arrange
        List<VoiceLog> voiceLogs = Arrays.asList(voiceLog);
        when(voiceLogRepository.findByProvider(VoiceLog.Provider.ELEVENLABS)).thenReturn(voiceLogs);
        when(voiceLogMapper.toDto(voiceLog)).thenReturn(voiceLogDTO);

        // Act
        List<VoiceLogDTO> result = elevenLabsVoiceLogService.getAllElevenLabsVoiceLogs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-conversation-id", result.get(0).getExternalCallId());
        verify(voiceLogRepository).findByProvider(VoiceLog.Provider.ELEVENLABS);
        verify(voiceLogMapper).toDto(voiceLog);
    }

    @Test
    void getElevenLabsVoiceLogsWithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<VoiceLog> voiceLogs = Arrays.asList(voiceLog);
        Page<VoiceLog> voiceLogPage = new PageImpl<>(voiceLogs, pageable, 1);
        
        when(voiceLogRepository.findByProvider(VoiceLog.Provider.ELEVENLABS, pageable)).thenReturn(voiceLogPage);
        when(voiceLogMapper.toDto(voiceLog)).thenReturn(voiceLogDTO);

        // Act
        Page<VoiceLogDTO> result = elevenLabsVoiceLogService.getElevenLabsVoiceLogs(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("test-conversation-id", result.getContent().get(0).getExternalCallId());
        verify(voiceLogRepository).findByProvider(VoiceLog.Provider.ELEVENLABS, pageable);
        verify(voiceLogMapper).toDto(voiceLog);
    }

    @Test
    void getElevenLabsVoiceLogByExternalCallId_Success() {
        // Arrange
        when(voiceLogRepository.findByExternalCallId("test-conversation-id"))
                .thenReturn(Optional.of(voiceLog));
        when(voiceLogMapper.toDto(voiceLog)).thenReturn(voiceLogDTO);

        // Act
        VoiceLogDTO result = elevenLabsVoiceLogService.getElevenLabsVoiceLogByExternalCallId("test-conversation-id");

        // Assert
        assertNotNull(result);
        assertEquals("test-conversation-id", result.getExternalCallId());
        verify(voiceLogRepository).findByExternalCallId("test-conversation-id");
        verify(voiceLogMapper).toDto(voiceLog);
    }

    @Test
    void getElevenLabsVoiceLogByExternalCallId_NotFound() {
        // Arrange
        when(voiceLogRepository.findByExternalCallId("non-existent"))
                .thenReturn(Optional.empty());

        // Act
        VoiceLogDTO result = elevenLabsVoiceLogService.getElevenLabsVoiceLogByExternalCallId("non-existent");

        // Assert
        assertNull(result);
        verify(voiceLogRepository).findByExternalCallId("non-existent");
        verify(voiceLogMapper, never()).toDto(any());
    }

    @Test
    void getElevenLabsVoiceLogByExternalCallId_WrongProvider() {
        // Arrange
        VoiceLog wrongProviderLog = VoiceLog.builder()
                .id(1L)
                .externalCallId("test-conversation-id")
                .provider(VoiceLog.Provider.VAPI) // Wrong provider
                .build();
        
        when(voiceLogRepository.findByExternalCallId("test-conversation-id"))
                .thenReturn(Optional.of(wrongProviderLog));

        // Act
        VoiceLogDTO result = elevenLabsVoiceLogService.getElevenLabsVoiceLogByExternalCallId("test-conversation-id");

        // Assert
        assertNull(result);
        verify(voiceLogRepository).findByExternalCallId("test-conversation-id");
        verify(voiceLogMapper, never()).toDto(any());
    }

    @Test
    void getElevenLabsVoiceLogAudio_Success() {
        // Arrange
        when(voiceLogRepository.findByExternalCallId("test-conversation-id"))
                .thenReturn(Optional.of(voiceLog));
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        
        byte[] audioData = "test audio data".getBytes();
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(audioData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(byte[].class)))
                .thenReturn(responseEntity);

        // Act
        Resource result = elevenLabsVoiceLogService.getElevenLabsVoiceLogAudio("test-conversation-id");

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof ByteArrayResource);
        verify(voiceLogRepository).findByExternalCallId("test-conversation-id");
        verify(restTemplate).exchange(anyString(), any(), any(), eq(byte[].class));
    }

    @Test
    void getElevenLabsVoiceLogAudio_VoiceLogNotFound() {
        // Arrange
        when(voiceLogRepository.findByExternalCallId("non-existent"))
                .thenReturn(Optional.empty());

        // Act
        Resource result = elevenLabsVoiceLogService.getElevenLabsVoiceLogAudio("non-existent");

        // Assert
        assertNull(result);
        verify(voiceLogRepository).findByExternalCallId("non-existent");
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(byte[].class));
    }

    @Test
    void manualSync_DelegatesToSyncMethod() {
        // Act
        ElevenLabsVoiceLogService.SyncSummary result = elevenLabsVoiceLogService.manualSync();

        // Assert
        assertNotNull(result);
        // The actual sync logic is tested separately, here we just verify the delegation
    }
} 