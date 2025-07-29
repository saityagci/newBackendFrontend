package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AgentCreateDTO;
import com.sfaai.sfaai.dto.AgentDTO;
import com.sfaai.sfaai.entity.Agent;
import com.sfaai.sfaai.entity.Agent.AgentStatus;
import com.sfaai.sfaai.entity.Agent.AgentType;
import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.repository.ClientRepository;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.AgentMapper;
import com.sfaai.sfaai.repository.AgentRepository;
import com.sfaai.sfaai.service.impl.AgentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AgentMapper agentMapper;

    @InjectMocks
    private AgentServiceImpl agentService;

    private Agent testAgent;
    private AgentDTO testAgentDTO;
    private AgentCreateDTO testAgentCreateDTO;
    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .fullName("Test Client")
                .email("client@example.com")
                .build();

        testAgent = Agent.builder()
                .id(1L)
                .name("Test Agent")
                .description("Test Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .client(testClient)
                .build();

        testAgentDTO = AgentDTO.builder()
                .id(1L)
                .name("Test Agent")
                .description("Test Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .clientId(1L)
                .build();

        testAgentCreateDTO = AgentCreateDTO.builder()
                .name("Test Agent")
                .description("Test Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .clientId(1L)
                .build();
    }

    @Test
    void createAgent_WithValidData_ShouldReturnCreatedAgent() {
        // Arrange
        when(agentMapper.createEntityFromDto(any(AgentCreateDTO.class))).thenReturn(testAgent);
        when(agentRepository.save(any(Agent.class))).thenReturn(testAgent);
        when(agentMapper.toDto(any(Agent.class))).thenReturn(testAgentDTO);

        // Act
        AgentDTO result = agentService.createAgent(testAgentCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Agent", result.getName());
        assertEquals(AgentType.VAPI, result.getType());
        assertEquals(AgentStatus.ACTIVE, result.getStatus());

        verify(agentMapper).createEntityFromDto(testAgentCreateDTO);
        verify(agentRepository).save(testAgent);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void createAgent_WithNullData_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            agentService.createAgent(null);
        });

        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void getAgentById_WithValidId_ShouldReturnAgent() {
        // Arrange
        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(agentMapper.toDto(testAgent)).thenReturn(testAgentDTO);

        // Act
        AgentDTO result = agentService.getAgentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Agent", result.getName());

        verify(agentRepository).findById(1L);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void getAgentById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(agentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            agentService.getAgentById(999L);
        });

        verify(agentRepository).findById(999L);
        verify(agentMapper, never()).toDto(any(Agent.class));
    }

    @Test
    void getAllAgents_ShouldReturnAllAgents() {
        // Arrange
        List<Agent> agents = Arrays.asList(testAgent);
        List<AgentDTO> agentDTOs = Arrays.asList(testAgentDTO);
        when(agentRepository.findAll()).thenReturn(agents);
        when(agentMapper.toDtoList(agents)).thenReturn(agentDTOs);

        // Act
        List<AgentDTO> result = agentService.getAllAgents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Agent", result.get(0).getName());

        verify(agentRepository).findAll();
        verify(agentMapper).toDtoList(agents);
    }

    @Test
    void getAllAgents_WithPagination_ShouldReturnPaginatedAgents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Agent> agents = Arrays.asList(testAgent);
        Page<Agent> agentPage = new PageImpl<>(agents, pageable, 1);
        when(agentRepository.findAll(pageable)).thenReturn(agentPage);
        when(agentMapper.toDto(testAgent)).thenReturn(testAgentDTO);

        // Act
        Page<AgentDTO> result = agentService.getAgents(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Test Agent", result.getContent().get(0).getName());

        verify(agentRepository).findAll(pageable);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void updateAgent_WithValidData_ShouldReturnUpdatedAgent() {
        // Arrange
        AgentDTO updatedDTO = AgentDTO.builder()
                .id(1L)
                .name("Updated Agent")
                .description("Updated Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .build();

        Agent updatedAgent = Agent.builder()
                .id(1L)
                .name("Updated Agent")
                .description("Updated Description")
                .type(AgentType.VAPI)
                .status(AgentStatus.ACTIVE)
                .build();

        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(agentRepository.save(any(Agent.class))).thenReturn(updatedAgent);
        when(agentMapper.toDto(updatedAgent)).thenReturn(updatedDTO);

        // Act
        AgentDTO result = agentService.updateAgent(1L, updatedDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Agent", result.getName());
        assertEquals("Updated Description", result.getDescription());

        verify(agentRepository).findById(1L);
        verify(agentRepository).save(any(Agent.class));
        verify(agentMapper).toDto(updatedAgent);
    }

    @Test
    void updateAgent_WithInvalidId_ShouldThrowException() {
        // Arrange
        AgentDTO updatedDTO = AgentDTO.builder()
                .id(999L)
                .name("Updated Agent")
                .build();

        when(agentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            agentService.updateAgent(999L, updatedDTO);
        });

        verify(agentRepository).findById(999L);
        verify(agentRepository, never()).save(any(Agent.class));
    }

    @Test
    void deleteAgent_WithValidId_ShouldDeleteAgent() {
        // Arrange
        when(agentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(agentRepository).deleteById(1L);

        // Act
        agentService.deleteAgent(1L);

        // Assert
        verify(agentRepository).existsById(1L);
        verify(agentRepository).deleteById(1L);
    }

    @Test
    void deleteAgent_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(agentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            agentService.deleteAgent(999L);
        });

        verify(agentRepository).existsById(999L);
        verify(agentRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAgentsByClientId_ShouldReturnClientAgents() {
        // Arrange
        List<Agent> agents = Arrays.asList(testAgent);
        List<AgentDTO> agentDTOs = Arrays.asList(testAgentDTO);
        when(clientRepository.existsById(1L)).thenReturn(true);
        when(agentRepository.findByClient_Id(1L)).thenReturn(agents);
        when(agentMapper.toDto(testAgent)).thenReturn(testAgentDTO);

        // Act
        List<AgentDTO> result = agentService.getAgentsByClientId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Agent", result.get(0).getName());

        verify(agentRepository).findByClient_Id(1L);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void getAgentsByClientId_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        List<Agent> agents = Arrays.asList(testAgent);
        Page<Agent> agentPage = new PageImpl<>(agents);
        Pageable pageable = PageRequest.of(0, 10);

        when(agentRepository.findByClient_Id(1L, pageable)).thenReturn(agentPage);

        Page<AgentDTO> result = agentService.getAgentsByClientId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(agentRepository).findByClient_Id(1L, pageable);
    }

    @Test
    void getAgentsByType_ShouldReturnAgentsOfType() {
        // Arrange
        List<Agent> agents = Arrays.asList(testAgent);
        List<AgentDTO> agentDTOs = Arrays.asList(testAgentDTO);
        when(agentRepository.findByType(AgentType.VAPI)).thenReturn(agents);
        when(agentMapper.toDto(testAgent)).thenReturn(testAgentDTO);

        // Act
        List<AgentDTO> result = agentService.getAgentsByType(AgentType.VAPI);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AgentType.VAPI, result.get(0).getType());

        verify(agentRepository).findByType(AgentType.VAPI);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void getAgentsByStatus_ShouldReturnAgentsOfStatus() {
        // Arrange
        List<Agent> agents = Arrays.asList(testAgent);
        List<AgentDTO> agentDTOs = Arrays.asList(testAgentDTO);
        when(agentRepository.findByStatus(AgentStatus.ACTIVE)).thenReturn(agents);
        when(agentMapper.toDto(testAgent)).thenReturn(testAgentDTO);

        // Act
        List<AgentDTO> result = agentService.getAgentsByStatus(AgentStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AgentStatus.ACTIVE, result.get(0).getStatus());

        verify(agentRepository).findByStatus(AgentStatus.ACTIVE);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void countAgentsByClientId_ShouldReturnCorrectCount() {
        // Arrange
        when(agentRepository.countByClient_Id(1L)).thenReturn(5L);

        // Act
        long result = agentService.countAgentsByClientId(1L);

        // Assert
        assertEquals(5L, result);
        verify(agentRepository).countByClient_Id(1L);
    }

    @Test
    void updateAgentStatus_WithValidData_ShouldReturnUpdatedAgent() {
        // Arrange
        AgentDTO updatedDTO = AgentDTO.builder()
                .id(1L)
                .name("Test Agent")
                .status(AgentStatus.INACTIVE)
                .build();

        when(agentRepository.findById(1L)).thenReturn(Optional.of(testAgent));
        when(agentRepository.save(any(Agent.class))).thenReturn(testAgent);
        when(agentMapper.toDto(testAgent)).thenReturn(updatedDTO);

        // Act
        AgentDTO result = agentService.updateAgentStatus(1L, AgentStatus.INACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(AgentStatus.INACTIVE, result.getStatus());

        verify(agentRepository).findById(1L);
        verify(agentRepository).save(testAgent);
        verify(agentMapper).toDto(testAgent);
    }

    @Test
    void updateAgentStatus_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(agentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            agentService.updateAgentStatus(999L, AgentStatus.INACTIVE);
        });

        verify(agentRepository).findById(999L);
        verify(agentRepository, never()).save(any(Agent.class));
    }
} 