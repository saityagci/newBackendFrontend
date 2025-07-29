package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VoiceLogCreateDTO;
import com.sfaai.sfaai.dto.VoiceLogDTO;
import com.sfaai.sfaai.entity.VoiceLog;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.VoiceLogMapper;
import com.sfaai.sfaai.repository.VoiceLogRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.service.impl.VoiceLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceLogServiceTest {

    @Mock
    private VoiceLogRepository voiceLogRepository;
    @Mock
    private VoiceLogMapper voiceLogMapper;
    @Mock
    private VapiAssistantRepository vapiAssistantRepository;
    @Mock
    private ElevenLabsAssistantRepository elevenLabsAssistantRepository;

    @InjectMocks
    private VoiceLogServiceImpl voiceLogService;

    private VoiceLog testVoiceLog;
    private VoiceLogDTO testVoiceLogDTO;
    private VoiceLogCreateDTO testVoiceLogCreateDTO;

    @BeforeEach
    void setUp() {
        testVoiceLog = VoiceLog.builder()
                .id(1L)
                .audioUrl("https://example.com/audio.mp3")
                .status(com.sfaai.sfaai.entity.VoiceLog.Status.COMPLETED)
                .provider(com.sfaai.sfaai.entity.VoiceLog.Provider.VAPI)
                .build();

        testVoiceLogDTO = VoiceLogDTO.builder()
                .id(1L)
                .agentId(1L)
                .clientId(1L)
                .audioUrl("https://example.com/audio.mp3")
                .status("COMPLETED")
                .provider("VAPI")
                .build();

        testVoiceLogCreateDTO = VoiceLogCreateDTO.builder()
                .agentId(1L)
                .clientId(1L)
                .assistantId("test-assistant-id")
                .audioUrl("https://example.com/audio.mp3")
                .status(com.sfaai.sfaai.entity.VoiceLog.Status.COMPLETED)
                .provider("VAPI")
                .build();
    }

    @Test
    void createVoiceLog_ValidRequest_ShouldReturnCreatedVoiceLog() {
        // Arrange
        when(voiceLogMapper.createEntityFromDto(any(VoiceLogCreateDTO.class))).thenReturn(testVoiceLog);
        when(voiceLogRepository.save(any(VoiceLog.class))).thenReturn(testVoiceLog);
        when(voiceLogMapper.toDto(testVoiceLog)).thenReturn(testVoiceLogDTO);
        VapiAssistant mockVapiAssistant = VapiAssistant.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .build();
        when(vapiAssistantRepository.findById("test-assistant-id")).thenReturn(Optional.of(mockVapiAssistant));

        // Act
        VoiceLogDTO result = voiceLogService.createVoiceLog(testVoiceLogCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testVoiceLogDTO.getId(), result.getId());
        assertEquals(testVoiceLogDTO.getAgentId(), result.getAgentId());
        assertEquals(testVoiceLogDTO.getClientId(), result.getClientId());
        verify(voiceLogMapper).createEntityFromDto(testVoiceLogCreateDTO);
        verify(voiceLogRepository).save(testVoiceLog);
        verify(voiceLogMapper).toDto(testVoiceLog);
    }

    @Test
    void createVoiceLog_NullRequest_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> voiceLogService.createVoiceLog(null));
        verify(voiceLogRepository, never()).save(any(VoiceLog.class));
    }

    @Test
    void createVoiceLog_RepositoryException_ShouldThrowException() {
        // Arrange
        when(voiceLogMapper.createEntityFromDto(any(VoiceLogCreateDTO.class))).thenReturn(testVoiceLog);
        when(voiceLogRepository.save(any(VoiceLog.class))).thenThrow(new RuntimeException("Database error"));
        VapiAssistant mockVapiAssistant = VapiAssistant.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .build();
        when(vapiAssistantRepository.findById("test-assistant-id")).thenReturn(Optional.of(mockVapiAssistant));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> voiceLogService.createVoiceLog(testVoiceLogCreateDTO));
        verify(voiceLogMapper).createEntityFromDto(testVoiceLogCreateDTO);
        verify(voiceLogRepository).save(any(VoiceLog.class));
    }

    @Test
    void getVoiceLogsByClientId_ValidClientId_ShouldReturnVoiceLogs() {
        // Arrange
        List<VoiceLog> voiceLogs = Arrays.asList(testVoiceLog);
        List<VoiceLogDTO> voiceLogDTOs = Arrays.asList(testVoiceLogDTO);
        when(voiceLogRepository.findByClient_Id(1L)).thenReturn(voiceLogs);
        when(voiceLogMapper.toDtoList(voiceLogs)).thenReturn(voiceLogDTOs);

        // Act
        List<VoiceLogDTO> result = voiceLogService.getVoiceLogsByClientId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVoiceLogDTO.getId(), result.get(0).getId());
        verify(voiceLogRepository).findByClient_Id(1L);
        verify(voiceLogMapper).toDtoList(voiceLogs);
    }

    @Test
    void getVoiceLogsByClientId_WhenNoLogsFound_ShouldReturnEmptyList() {
        // Arrange
        when(voiceLogRepository.findByClient_Id(999L)).thenReturn(Arrays.asList());

        // Act
        List<VoiceLogDTO> result = voiceLogService.getVoiceLogsByClientId(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(voiceLogRepository).findByClient_Id(999L);
    }

    @Test
    void getVoiceLogsByClientId_WhenClientIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            voiceLogService.getVoiceLogsByClientId(null);
        });
        verify(voiceLogRepository, never()).findByClient_Id(any());
    }

    @Test
    void getVoiceLogsByClientId_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        when(voiceLogRepository.findByClient_Id(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            voiceLogService.getVoiceLogsByClientId(1L);
        });
        verify(voiceLogRepository).findByClient_Id(1L);
    }

    @Test
    void getAllVoiceLogs_ShouldReturnAllVoiceLogs() {
        // Arrange
        List<VoiceLog> voiceLogs = Arrays.asList(testVoiceLog);
        List<VoiceLogDTO> voiceLogDTOs = Arrays.asList(testVoiceLogDTO);
        when(voiceLogRepository.findAll()).thenReturn(voiceLogs);
        when(voiceLogMapper.toDtoList(voiceLogs)).thenReturn(voiceLogDTOs);

        // Act
        List<VoiceLogDTO> result = voiceLogService.getAllVoiceLogs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVoiceLogDTO.getId(), result.get(0).getId());
        verify(voiceLogRepository).findAll();
        verify(voiceLogMapper).toDtoList(voiceLogs);
    }

    @Test
    void getAllVoiceLogs_EmptyDatabase_ShouldReturnEmptyList() {
        // Arrange
        when(voiceLogRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<VoiceLogDTO> result = voiceLogService.getAllVoiceLogs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(voiceLogRepository).findAll();
        verify(voiceLogMapper, never()).toDto(any(VoiceLog.class));
    }

    @Test
    void getAllVoiceLogs_RepositoryException_ShouldThrowException() {
        // Arrange
        when(voiceLogRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> voiceLogService.getAllVoiceLogs());
        verify(voiceLogRepository).findAll();
    }

    @Test
    void getVoiceLogById_ExistingVoiceLog_ShouldReturnVoiceLog() {
        // Arrange
        when(voiceLogRepository.findById(1L)).thenReturn(Optional.of(testVoiceLog));
        when(voiceLogMapper.toDto(testVoiceLog)).thenReturn(testVoiceLogDTO);

        // Act
        VoiceLogDTO result = voiceLogService.getVoiceLogById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVoiceLogDTO.getId(), result.getId());
        verify(voiceLogRepository).findById(1L);
        verify(voiceLogMapper).toDto(testVoiceLog);
    }

    @Test
    void getVoiceLogById_NonExistingVoiceLog_ShouldThrowException() {
        // Arrange
        when(voiceLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> voiceLogService.getVoiceLogById(999L));
        verify(voiceLogRepository).findById(999L);
    }

    @Test
    void getVoiceLogById_NullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> voiceLogService.getVoiceLogById(null));
        verify(voiceLogRepository).findById(null);
    }

    @Test
    void deleteVoiceLog_ExistingVoiceLog_ShouldDeleteSuccessfully() {
        // Arrange
        when(voiceLogRepository.findById(1L)).thenReturn(Optional.of(testVoiceLog));
        doNothing().when(voiceLogRepository).delete(testVoiceLog);

        // Act
        voiceLogService.deleteVoiceLog(1L);

        // Assert
        verify(voiceLogRepository).findById(1L);
        verify(voiceLogRepository).delete(testVoiceLog);
    }

    @Test
    void deleteVoiceLog_NonExistingVoiceLog_ShouldThrowException() {
        // Arrange
        when(voiceLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> voiceLogService.deleteVoiceLog(999L));
        verify(voiceLogRepository).findById(999L);
        verify(voiceLogRepository, never()).delete(any(VoiceLog.class));
    }

    @Test
    void deleteVoiceLog_NullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> voiceLogService.deleteVoiceLog(null));
        verify(voiceLogRepository).findById(null);
        verify(voiceLogRepository, never()).delete(any(VoiceLog.class));
    }
} 