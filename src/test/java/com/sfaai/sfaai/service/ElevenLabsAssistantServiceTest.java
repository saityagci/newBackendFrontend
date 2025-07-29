package com.sfaai.sfaai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfaai.sfaai.config.ElevenLabsConfig;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDTO;
import com.sfaai.sfaai.dto.ElevenLabsAssistantDetailResponse;
import com.sfaai.sfaai.dto.ElevenLabsListAssistantsResponse;
import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import com.sfaai.sfaai.mapper.ElevenLabsAssistantMapper;
import com.sfaai.sfaai.repository.ElevenLabsAssistantRepository;
import com.sfaai.sfaai.service.impl.ElevenLabsAssistantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevenLabsAssistantServiceTest {

    @Mock
    private ElevenLabsConfig elevenLabsConfig;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ElevenLabsAssistantRepository assistantRepository;
    @Mock
    private ElevenLabsAssistantMapper assistantMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ElevenLabsAssistantServiceImpl elevenLabsAssistantService;

    private ElevenLabsAssistant testAssistant;
    private ElevenLabsAssistantDTO testAssistantDTO;
    private ElevenLabsListAssistantsResponse testListResponse;
    private ElevenLabsAssistantDetailResponse testDetailResponse;

    @BeforeEach
    void setUp() {
        testAssistant = ElevenLabsAssistant.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .build();

        testAssistantDTO = ElevenLabsAssistantDTO.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .build();

        testListResponse = ElevenLabsListAssistantsResponse.builder()
                .assistants(Arrays.asList(testAssistantDTO))
                .build();

        testDetailResponse = ElevenLabsAssistantDetailResponse.builder()
                .assistantId("test-assistant-id")
                .name("Test Assistant")
                .build();
    }

    @Test
    void getAllAssistantsFromApi_Success_ShouldReturnAssistants() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        )).thenReturn(new ResponseEntity<>(testListResponse, HttpStatus.OK));

        // Act
        ElevenLabsListAssistantsResponse result = elevenLabsAssistantService.getAllAssistantsFromApi();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getAssistants().size());
        assertEquals(testAssistantDTO.getId(), result.getAssistants().get(0).getId());
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        );
    }

    @Test
    void getAllAssistantsFromApi_Unauthorized_ShouldThrowException() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("invalid-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> elevenLabsAssistantService.getAllAssistantsFromApi());
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
    }

    @Test
    void getAllAssistantsFromApi_ServerError_ShouldThrowException() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(HttpServerErrorException.class, () -> elevenLabsAssistantService.getAllAssistantsFromApi());
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
    }

    @Test
    void getAllAssistants_Success_ShouldReturnAssistantsFromDatabase() {
        // Arrange
        List<ElevenLabsAssistant> assistants = Arrays.asList(testAssistant);
        when(assistantRepository.findAll()).thenReturn(assistants);
        when(assistantMapper.toDto(testAssistant)).thenReturn(testAssistantDTO);

        // Act
        List<ElevenLabsAssistantDTO> result = elevenLabsAssistantService.getAllAssistants();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssistantDTO.getId(), result.get(0).getId());
        verify(assistantRepository).findAll();
        verify(assistantMapper).toDto(testAssistant);
    }

    @Test
    void getAllAssistants_EmptyDatabase_ShouldReturnEmptyList() {
        // Arrange
        when(assistantRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ElevenLabsAssistantDTO> result = elevenLabsAssistantService.getAllAssistants();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(assistantRepository).findAll();
    }

    @Test
    void getAssistant_ExistingAssistant_ShouldReturnAssistant() {
        // Arrange
        when(assistantRepository.findById("test-assistant-id")).thenReturn(Optional.of(testAssistant));
        when(assistantMapper.toDto(testAssistant)).thenReturn(testAssistantDTO);

        // Act
        ElevenLabsAssistantDTO result = elevenLabsAssistantService.getAssistant("test-assistant-id");

        // Assert
        assertNotNull(result);
        assertEquals(testAssistantDTO.getId(), result.getId());
        verify(assistantRepository).findById("test-assistant-id");
        verify(assistantMapper).toDto(testAssistant);
    }

    @Test
    void getAssistant_NonExistingAssistant_ShouldReturnNull() {
        // Arrange
        when(assistantRepository.findById("non-existing-id")).thenReturn(Optional.empty());

        // Act
        ElevenLabsAssistantDTO result = elevenLabsAssistantService.getAssistant("non-existing-id");

        // Assert
        assertNull(result);
        verify(assistantRepository).findById("non-existing-id");
        verify(assistantMapper, never()).toDto(any());
    }

    @Test
    void getAssistantDetailsFromApi_Success_ShouldReturnAssistantDetails() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsAssistantDetailResponse.class)
        )).thenReturn(new ResponseEntity<>(testDetailResponse, HttpStatus.OK));

        // Act
        ElevenLabsAssistantDetailResponse result = elevenLabsAssistantService.getAssistantDetailsFromApi("test-assistant-id");

        // Assert
        assertNotNull(result);
        assertEquals(testDetailResponse.getId(), result.getId());
        assertEquals(testDetailResponse.getName(), result.getName());
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsAssistantDetailResponse.class)
        );
    }

    @Test
    void getAssistantDetailsFromApi_NotFound_ShouldThrowException() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsAssistantDetailResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> elevenLabsAssistantService.getAssistantDetailsFromApi("non-existing-id"));
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
    }

    @Test
    void syncAllAssistants_Success_ShouldReturnNumberOfSyncedAssistants() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("test-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        )).thenReturn(new ResponseEntity<>(testListResponse, HttpStatus.OK));
        when(assistantRepository.findById(anyString())).thenReturn(Optional.empty());
        when(assistantRepository.save(any(ElevenLabsAssistant.class))).thenReturn(testAssistant);

        // Act
        int result = elevenLabsAssistantService.syncAllAssistants();

        // Assert
        assertEquals(1, result);
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        );
        verify(assistantRepository).findById(anyString());
        verify(assistantRepository).save(any(ElevenLabsAssistant.class));
    }

    @Test
    void syncAllAssistants_ApiError_ShouldThrowException() {
        // Arrange
        when(elevenLabsConfig.getApiKey()).thenReturn("invalid-api-key");
        when(elevenLabsConfig.getApiUrl()).thenReturn("https://api.elevenlabs.io");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ElevenLabsListAssistantsResponse.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> elevenLabsAssistantService.syncAllAssistants());
        verify(elevenLabsConfig).getApiKey();
        verify(elevenLabsConfig).getApiUrl();
    }
} 