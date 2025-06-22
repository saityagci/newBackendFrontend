package com.sfaai.sfaai.scheduler;

import com.sfaai.sfaai.service.VapiAssistantSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VapiAssistantSyncSchedulerTest {

    @Mock
    private VapiAssistantSyncService vapiAssistantSyncService;

    @InjectMocks
    private VapiAssistantSyncScheduler scheduler;

    @Test
    void scheduledSync_ShouldCallSynchronizeAllAssistants() {
        // Arrange
        when(vapiAssistantSyncService.synchronizeAllAssistants()).thenReturn(5);
        ReflectionTestUtils.setField(scheduler, "syncInterval", 600000L);

        // Act
        scheduler.scheduledSync();

        // Assert
        verify(vapiAssistantSyncService, times(1)).synchronizeAllAssistants();
    }

    @Test
    void scheduledSync_ShouldHandleExceptions() {
        // Arrange
        when(vapiAssistantSyncService.synchronizeAllAssistants()).thenThrow(new RuntimeException("Test exception"));

        // Act - should not throw exception
        scheduler.scheduledSync();

        // Assert
        verify(vapiAssistantSyncService, times(1)).synchronizeAllAssistants();
    }
}
