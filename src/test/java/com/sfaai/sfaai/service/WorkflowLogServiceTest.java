package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.WorkflowLogCreateDTO;
import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.WorkflowLog;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.WorkflowLogMapper;
import com.sfaai.sfaai.repository.WorkflowLogRepository;
import com.sfaai.sfaai.service.impl.WorkflowLogServiceImpl;
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
class WorkflowLogServiceTest {

    @Mock
    private WorkflowLogRepository workflowLogRepository;
    @Mock
    private WorkflowLogMapper workflowLogMapper;

    @InjectMocks
    private WorkflowLogServiceImpl workflowLogService;

    private WorkflowLog testWorkflowLog;
    private WorkflowLogDTO testWorkflowLogDTO;
    private WorkflowLogCreateDTO testWorkflowLogCreateDTO;

    @BeforeEach
    void setUp() {
        testWorkflowLog = WorkflowLog.builder()
                .id(1L)
                .workflowName("Test Workflow")
                .status(com.sfaai.sfaai.entity.WorkflowLog.Status.COMPLETED)
                .build();

        testWorkflowLogDTO = WorkflowLogDTO.builder()
                .id(1L)
                .agentId(1L)
                .clientId(1L)
                .workflowName("Test Workflow")
                .status("COMPLETED")
                .build();

        testWorkflowLogCreateDTO = WorkflowLogCreateDTO.builder()
                .agentId(1L)
                .clientId(1L)
                .workflowName("Test Workflow")
                .status("COMPLETED")
                .build();
    }

    @Test
    void createWorkflowLog_ValidRequest_ShouldReturnCreatedWorkflowLog() {
        // Arrange
        when(workflowLogMapper.createEntityFromDto(any(WorkflowLogCreateDTO.class))).thenReturn(testWorkflowLog);
        when(workflowLogRepository.save(any(WorkflowLog.class))).thenReturn(testWorkflowLog);
        when(workflowLogMapper.toDto(testWorkflowLog)).thenReturn(testWorkflowLogDTO);

        // Act
        WorkflowLogDTO result = workflowLogService.createWorkflowLog(testWorkflowLogCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testWorkflowLogDTO.getId(), result.getId());
        assertEquals(testWorkflowLogDTO.getAgentId(), result.getAgentId());
        assertEquals(testWorkflowLogDTO.getClientId(), result.getClientId());
        verify(workflowLogMapper).createEntityFromDto(testWorkflowLogCreateDTO);
        verify(workflowLogRepository).save(testWorkflowLog);
        verify(workflowLogMapper).toDto(testWorkflowLog);
    }

    @Test
    void createWorkflowLog_NullRequest_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> workflowLogService.createWorkflowLog(null));
        verify(workflowLogRepository, never()).save(any(WorkflowLog.class));
    }

    @Test
    void createWorkflowLog_RepositoryException_ShouldThrowException() {
        // Arrange
        when(workflowLogMapper.createEntityFromDto(any(WorkflowLogCreateDTO.class))).thenReturn(testWorkflowLog);
        when(workflowLogRepository.save(any(WorkflowLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> workflowLogService.createWorkflowLog(testWorkflowLogCreateDTO));
        verify(workflowLogMapper).createEntityFromDto(testWorkflowLogCreateDTO);
        verify(workflowLogRepository).save(testWorkflowLog);
    }

    @Test
    void getWorkflowLogsByClientId_ValidClientId_ShouldReturnWorkflowLogs() {
        // Arrange
        List<WorkflowLog> workflowLogs = Arrays.asList(testWorkflowLog);
        when(workflowLogRepository.findByClient_Id(1L)).thenReturn(workflowLogs);
        when(workflowLogMapper.toDto(testWorkflowLog)).thenReturn(testWorkflowLogDTO);

        // Act
        List<WorkflowLogDTO> result = workflowLogService.getWorkflowLogsByClientId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkflowLogDTO.getId(), result.get(0).getId());
        verify(workflowLogRepository).findByClient_Id(1L);
        verify(workflowLogMapper).toDto(testWorkflowLog);
    }

    @Test
    void getWorkflowLogsByClientId_WhenNoLogsFound_ShouldReturnEmptyList() {
        // Arrange
        when(workflowLogRepository.findByClient_Id(999L)).thenReturn(Arrays.asList());

        // Act
        List<WorkflowLogDTO> result = workflowLogService.getWorkflowLogsByClientId(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workflowLogRepository).findByClient_Id(999L);
    }

    @Test
    void getWorkflowLogsByClientId_WhenClientIdIsNull_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            workflowLogService.getWorkflowLogsByClientId(null);
        });
        verify(workflowLogRepository, never()).findByClient_Id(any());
    }

    @Test
    void getWorkflowLogsByClientId_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        when(workflowLogRepository.findByClient_Id(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            workflowLogService.getWorkflowLogsByClientId(1L);
        });
        verify(workflowLogRepository).findByClient_Id(1L);
    }

    @Test
    void getAllWorkflowLogs_ShouldReturnAllWorkflowLogs() {
        // Arrange
        List<WorkflowLog> workflowLogs = Arrays.asList(testWorkflowLog);
        when(workflowLogRepository.findAll()).thenReturn(workflowLogs);
        when(workflowLogMapper.toDto(testWorkflowLog)).thenReturn(testWorkflowLogDTO);

        // Act
        List<WorkflowLogDTO> result = workflowLogService.getAllWorkflowLogs();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkflowLogDTO.getId(), result.get(0).getId());
        verify(workflowLogRepository).findAll();
        verify(workflowLogMapper).toDto(testWorkflowLog);
    }

    @Test
    void getAllWorkflowLogs_EmptyDatabase_ShouldReturnEmptyList() {
        // Arrange
        when(workflowLogRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<WorkflowLogDTO> result = workflowLogService.getAllWorkflowLogs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(workflowLogRepository).findAll();
        verify(workflowLogMapper, never()).toDto(any(WorkflowLog.class));
    }

    @Test
    void getAllWorkflowLogs_RepositoryException_ShouldThrowException() {
        // Arrange
        when(workflowLogRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> workflowLogService.getAllWorkflowLogs());
        verify(workflowLogRepository).findAll();
    }

    @Test
    void getWorkflowLogById_ExistingWorkflowLog_ShouldReturnWorkflowLog() {
        // Arrange
        when(workflowLogRepository.findById(1L)).thenReturn(Optional.of(testWorkflowLog));
        when(workflowLogMapper.toDto(testWorkflowLog)).thenReturn(testWorkflowLogDTO);

        // Act
        WorkflowLogDTO result = workflowLogService.getWorkflowLogById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testWorkflowLogDTO.getId(), result.getId());
        verify(workflowLogRepository).findById(1L);
        verify(workflowLogMapper).toDto(testWorkflowLog);
    }

    @Test
    void getWorkflowLogById_NonExistingWorkflowLog_ShouldThrowException() {
        // Arrange
        when(workflowLogRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workflowLogService.getWorkflowLogById(999L));
        verify(workflowLogRepository).findById(999L);
        verify(workflowLogMapper, never()).toDto(any(WorkflowLog.class));
    }

    @Test
    void getWorkflowLogById_NullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> workflowLogService.getWorkflowLogById(null));
        verify(workflowLogRepository, never()).findById(any());
    }

    @Test
    void deleteWorkflowLog_ExistingWorkflowLog_ShouldDeleteSuccessfully() {
        // Arrange
        when(workflowLogRepository.existsById(1L)).thenReturn(true);
        doNothing().when(workflowLogRepository).deleteById(1L);

        // Act
        workflowLogService.deleteWorkflowLog(1L);

        // Assert
        verify(workflowLogRepository).existsById(1L);
        verify(workflowLogRepository).deleteById(1L);
    }

    @Test
    void deleteWorkflowLog_NonExistingWorkflowLog_ShouldThrowException() {
        // Arrange
        when(workflowLogRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workflowLogService.deleteWorkflowLog(999L));
        verify(workflowLogRepository).existsById(999L);
        verify(workflowLogRepository, never()).deleteById(any());
    }

    @Test
    void deleteWorkflowLog_NullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> workflowLogService.deleteWorkflowLog(null));
        verify(workflowLogRepository, never()).existsById(any());
        verify(workflowLogRepository, never()).deleteById(any());
    }
} 