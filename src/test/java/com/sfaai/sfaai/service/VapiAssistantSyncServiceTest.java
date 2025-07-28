package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.VapiAssistantDTO;
import com.sfaai.sfaai.dto.VapiListAssistantsResponse;
import com.sfaai.sfaai.entity.SyncStatus;
import com.sfaai.sfaai.entity.VapiAssistant;
import com.sfaai.sfaai.mapper.VapiAssistantMapper;
import com.sfaai.sfaai.repository.SyncStatusRepository;
import com.sfaai.sfaai.repository.VapiAssistantRepository;
import com.sfaai.sfaai.service.impl.VapiAssistantSyncServiceImpl;
import com.sfaai.sfaai.util.FirstMessageFallbackAdapter;
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

    @Mock
    private FirstMessageFallbackAdapter firstMessageFallbackAdapter;

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
        when(vapiAssistantMapper.toEntity(any(VapiAssistantDTO.class)))
                .thenReturn(assistant1)
                .thenReturn(assistant2);
        when(vapiAssistantRepository.saveAll(anyList())).thenReturn(Arrays.asList(assistant1, assistant2));
        when(syncStatusRepository.save(any(SyncStatus.class))).thenReturn(new SyncStatus());
        lenient().doNothing().when(firstMessageFallbackAdapter).applyFallbackMessage(any(VapiAssistant.class));

        // Act
        int result = vapiAssistantSyncService.synchronizeAllAssistants();

        // Assert
        assertEquals(2, result);
        verify(vapiAssistantRepository).saveAll(assistantsCaptor.capture());
        verify(syncStatusRepository, times(2)).save(syncStatusCaptor.capture());
        
        List<VapiAssistant> savedAssistants = assistantsCaptor.getValue();
        assertEquals(2, savedAssistants.size());
    }

    @Test
    void synchronizeAllAssistants_NoAssistantsFound() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Collections.emptyList());

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(syncStatusRepository.save(any(SyncStatus.class))).thenReturn(new SyncStatus());

        // Act
        int result = vapiAssistantSyncService.synchronizeAllAssistants();

        // Assert
        assertEquals(0, result);
        verify(vapiAssistantRepository, never()).saveAll(anyList());
        verify(syncStatusRepository, times(1)).save(syncStatusCaptor.capture());
    }

    @Test
    void synchronizeAssistant_Success() {
        // Arrange
        String assistantId = "assistant-1";
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(vapiAssistantMapper.toEntity(any(VapiAssistantDTO.class))).thenReturn(assistant1);
        when(vapiAssistantRepository.save(any(VapiAssistant.class))).thenReturn(assistant1);
        lenient().doNothing().when(firstMessageFallbackAdapter).applyFallbackMessage(any(VapiAssistant.class));

        // Act
        boolean result = vapiAssistantSyncService.synchronizeAssistant(assistantId);

        // Assert
        assertTrue(result);
        verify(vapiAssistantRepository).save(assistant1);
    }

    @Test
    void synchronizeAssistant_NotFound() {
        // Arrange
        String assistantId = "non-existent";
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Collections.emptyList());

        when(vapiAgentService.getAllAssistants()).thenReturn(response);

        // Act
        boolean result = vapiAssistantSyncService.synchronizeAssistant(assistantId);

        // Assert
        assertFalse(result);
        verify(vapiAssistantRepository, never()).save(any(VapiAssistant.class));
    }

    @Test
    void synchronizeAllAssistants_DatabaseError() {
        // Arrange
        VapiListAssistantsResponse response = new VapiListAssistantsResponse();
        response.setAssistants(Arrays.asList(assistantDTO1));

        when(vapiAgentService.getAllAssistants()).thenReturn(response);
        when(vapiAssistantMapper.toEntity(any(VapiAssistantDTO.class))).thenReturn(assistant1);
        when(vapiAssistantRepository.saveAll(anyList())).thenThrow(new DataAccessException("Database error") {});
        when(syncStatusRepository.save(any(SyncStatus.class))).thenReturn(new SyncStatus());
        lenient().doNothing().when(firstMessageFallbackAdapter).applyFallbackMessage(any(VapiAssistant.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            vapiAssistantSyncService.synchronizeAllAssistants();
        });
    }
}
