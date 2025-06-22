package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.SyncStatus;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.mapper.VapiAssistantMapper;
import com.sfaai.sfaai.repository.SyncStatusRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.impl.VapiAssistantSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VapiAssistantSyncServiceTest {

    @Mock
    private VapiAgentService vapiAgentService;

    @Mock
    private VapiAssistantRepository vapiAssistantRepository;

    @Mock
    private VapiAssistantMapper vapiAssistantMapper;

    @Mock
    private SyncStatusRepository syncStatusRepository;

    @InjectMocks
    private VapiAssistantSyncServiceImpl vapiAssistantSyncService;

    @Captor
    private ArgumentCaptor<List<VapiAssistant>> assistantsCaptor;

    @Captor
    private ArgumentCaptor<SyncStatus> syncStatusCaptor;

    private VapiAssistantDTO assistantDTO1;
    private VapiAssistantDTO assistantDTO2;
    private VapiAssistant assistant1;
    private VapiAssistant assistant2;

    @BeforeEach
    void setUp() {
        // Setup test data
        assistantDTO1 = new VapiAssistantDTO();
        assistantDTO1.setAssistantId("assistant-1");
        assistantDTO1.setName("Test Assistant 1");
        assistantDTO1.setStatus("active");

        assistantDTO2 = new VapiAssistantDTO();
        assistantDTO2.setAssistantId("assistant-2");
        assistantDTO2.setName("Test Assistant 2");
        assistantDTO2.setStatus("active");

        assistant1 = new VapiAssistant();
        assistant1.setAssistantId("assistant-1");
        assistant1.setName("Test Assistant 1");
        assistant1.setStatus("active");

        assistant2 = new VapiAssistant();
        assistant2.setAssistantId("assistant-2");
        assistant2.setName("Test Assistant 2");
        assistant2.setStatus("active");
    }

    @Test
    void synchronizeAllAssistants_Success() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1, assistantDTO2));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(vapiAssistantRepository.findByAssistantIdIn(anyList()))
                .thenReturn(Collections.singletonList(assistant1)); // Only assistant1 exists
        when(vapiAssistantMapper.toEntity(assistantDTO2)).thenReturn(assistant2);
        when(vapiAssistantRepository.saveAll(any())).thenReturn(Arrays.asList(assistant1, assistant2));

        // Act
        int result = vapiAssistantSyncService.synchronizeAllAssistants();

        // Assert
        assertEquals(2, result);
        verify(vapiAssistantRepository).saveAll(assistantsCaptor.capture());
        verify(syncStatusRepository, times(2)).save(syncStatusCaptor.capture());

        List<VapiAssistant> savedAssistants = assistantsCaptor.getValue();
        assertEquals(2, savedAssistants.size());
        assertTrue(savedAssistants.stream().anyMatch(a -> a.getAssistantId().equals("assistant-1")));
        assertTrue(savedAssistants.stream().anyMatch(a -> a.getAssistantId().equals("assistant-2")));

        // Check sync status was properly updated
        List<SyncStatus> statuses = syncStatusCaptor.getAllValues();
        assertEquals(2, statuses.size()); // Initial + final update
        assertTrue(statuses.get(1).isSuccess());
        assertEquals(2, statuses.get(1).getItemsProcessed());
        assertNotNull(statuses.get(1).getEndTime());
    }

    @Test
    void synchronizeAllAssistants_NoAssistantsFound() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Collections.emptyList());

        when(vapiAgentService.getAllAssistants()).thenReturn(response);

        // Act
        int result = vapiAssistantSyncService.synchronizeAllAssistants();

        // Assert
        assertEquals(0, result);
        verify(vapiAssistantRepository, never()).saveAll(any());
        verify(syncStatusRepository, times(2)).save(any()); // Initial + final update
    }

    @Test
    void synchronizeAssistant_Success() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1, assistantDTO2));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(vapiAssistantRepository.findById("assistant-1")).thenReturn(Optional.of(assistant1));

        // Act
        boolean result = vapiAssistantSyncService.synchronizeAssistant("assistant-1");

        // Assert
        assertTrue(result);
        verify(vapiAssistantRepository).save(any(VapiAssistant.class));
    }

    @Test
    void synchronizeAssistant_NotFound() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1, assistantDTO2));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);

        // Act
        boolean result = vapiAssistantSyncService.synchronizeAssistant("non-existent");

        // Assert
        assertFalse(result);
        verify(vapiAssistantRepository, never()).save(any(VapiAssistant.class));
    }

    @Test
    void synchronizeAllAssistants_HandlesException() {
        // Arrange
        when(vapiAgentService.getAllAssistants()).thenThrow(new RuntimeException("API error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vapiAssistantSyncService.synchronizeAllAssistants();
        });

        assertTrue(exception.getMessage().contains("Failed to synchronize"));
        verify(syncStatusRepository, times(2)).save(syncStatusCaptor.capture());

        // Check final sync status was marked as failed
        SyncStatus finalStatus = syncStatusCaptor.getValue();
        assertFalse(finalStatus.isSuccess());
        assertNotNull(finalStatus.getErrorDetails());
    }

    @Test
    void synchronizeAllAssistants_HandlesDbException() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1, assistantDTO2));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(vapiAssistantRepository.findByAssistantIdIn(anyList()))
                .thenReturn(Collections.emptyList());
        when(vapiAssistantMapper.toEntity(any())).thenReturn(assistant1, assistant2);
        when(vapiAssistantRepository.saveAll(any()))
                .thenThrow(new DataAccessException("DB error") {});

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            vapiAssistantSyncService.synchronizeAllAssistants();
        });

        assertTrue(exception.getMessage().contains("Failed to synchronize"));
        verify(syncStatusRepository, times(2)).save(any()); // Initial + error update
    }
}
