package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.AssistantConfigurationDTO;
import com.sfaai.sfaai.entity.AssistantConfiguration;
import com.sfaai.sfaai.mapper.AssistantConfigurationMapper;
import com.sfaai.sfaai.repository.AssistantConfigurationRepository;

import com.sfaai.sfaai.service.AssistantConfigurationService;
import com.sfaai.sfaai.service.AssistantDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssistantConfigurationServiceImpl implements AssistantConfigurationService {

    private final AssistantConfigurationRepository assistantConfigurationRepository;
    private final AssistantDocumentService assistantDocumentService;
    private final AssistantConfigurationMapper assistantConfigurationMapper;

    @Override
    public AssistantConfigurationDTO createConfiguration(String assistantId, String subject, String description,
                                                         List<MultipartFile> documents, String clientId, String clientEmail, String clientName) {
        log.info("Creating configuration for assistant: {}", assistantId);

        // Check if configuration already exists
        if (existsByAssistantId(assistantId)) {
            throw new IllegalArgumentException("Configuration already exists for assistant: " + assistantId);
        }

        // Create the configuration
        AssistantConfiguration configuration = AssistantConfiguration.builder()
                .assistantId(assistantId)
                .subject(subject)
                .description(description)
                .status(AssistantConfiguration.Status.PENDING)
                .clientId(clientId)
                .clientEmail(clientEmail)
                .clientName(clientName)
                .build();

        AssistantConfiguration savedConfiguration = assistantConfigurationRepository.save(configuration);

        // Upload documents if provided
        if (documents != null && !documents.isEmpty()) {
            assistantDocumentService.uploadDocuments(assistantId, documents, clientId);
        }

        log.info("Configuration created successfully for assistant: {}", assistantId);
        return assistantConfigurationMapper.toDto(savedConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public AssistantConfigurationDTO getConfigurationByAssistantId(String assistantId) {
        log.info("Getting configuration for assistant: {}", assistantId);
        
        AssistantConfiguration configuration = assistantConfigurationRepository.findByAssistantId(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found for assistant: " + assistantId));

        AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
        
        // Add documents to the response
        dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(assistantId));
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssistantConfigurationDTO> getConfigurationsByClientId(String clientId) {
        log.info("Getting configurations for client: {}", clientId);
        
        List<AssistantConfiguration> configurations = assistantConfigurationRepository.findByClientId(clientId);
        return configurations.stream()
                .map(configuration -> {
                    AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
                    // Add documents to the response
                    dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> getConfigurationsByClientId(String clientId, Pageable pageable) {
        log.info("Getting paginated configurations for client: {}", clientId);
        
        Page<AssistantConfiguration> configurations = assistantConfigurationRepository.findByClientId(clientId, pageable);
        return configurations.map(configuration -> {
            AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
            // Add documents to the response
            dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssistantConfigurationDTO> getConfigurationsByStatus(AssistantConfiguration.Status status) {
        log.info("Getting configurations with status: {}", status);
        
        List<AssistantConfiguration> configurations = assistantConfigurationRepository.findByStatus(status);
        return configurations.stream()
                .map(configuration -> {
                    AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
                    // Add documents to the response
                    dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> getConfigurationsByStatus(AssistantConfiguration.Status status, Pageable pageable) {
        log.info("Getting paginated configurations with status: {}", status);
        
        Page<AssistantConfiguration> configurations = assistantConfigurationRepository.findByStatusOrderByUpdatedAtDesc(status, pageable);
        return configurations.map(configuration -> {
            AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
            // Add documents to the response
            dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> getAllConfigurations(Pageable pageable) {
        return getAllConfigurations(pageable, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> getAllConfigurations(Pageable pageable, boolean includeDocuments) {
        log.info("Getting all configurations with pagination, includeDocuments: {}", includeDocuments);
        
        Page<AssistantConfiguration> configurations = assistantConfigurationRepository.findAll(pageable);
        return configurations.map(configuration -> {
            AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
            // Add documents to the response only if requested
            if (includeDocuments) {
                dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssistantConfigurationDTO> getAllConfigurations() {
        log.info("Getting all configurations");
        
        List<AssistantConfiguration> configurations = assistantConfigurationRepository.findAll();
        return configurations.stream()
                .map(configuration -> {
                    AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
                    // Add documents to the response
                    dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> searchConfigurations(String subject, String clientId, 
                                                               AssistantConfiguration.Status status, String assistantId, Pageable pageable) {
        log.info("Searching configurations with filters: subject={}, clientId={}, status={}, assistantId={}", 
                subject, clientId, status, assistantId);
        
        // Build dynamic query based on provided filters
        Page<AssistantConfiguration> configurations = assistantConfigurationRepository.findByFilters(
                subject, clientId, status, assistantId, pageable);
        return configurations.map(configuration -> {
            AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
            // Add documents to the response
            dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getConfigurationCounts() {
        log.info("Getting configuration counts by status");
        
        Map<String, Object> counts = new HashMap<>();
        counts.put("total", assistantConfigurationRepository.count());
        counts.put("pending", assistantConfigurationRepository.countByStatus(AssistantConfiguration.Status.PENDING));
        counts.put("open", assistantConfigurationRepository.countByStatus(AssistantConfiguration.Status.OPEN));
        counts.put("inProgress", assistantConfigurationRepository.countByStatus(AssistantConfiguration.Status.IN_PROGRESS));
        counts.put("resolved", assistantConfigurationRepository.countByStatus(AssistantConfiguration.Status.RESOLVED));
        counts.put("closed", assistantConfigurationRepository.countByStatus(AssistantConfiguration.Status.CLOSED));
        
        return counts;
    }

    @Override
    public AssistantConfigurationDTO updateConfigurationStatus(String assistantId, AssistantConfiguration.Status status) {
        log.info("Updating configuration status for assistant: {} to {}", assistantId, status);
        
        AssistantConfiguration configuration = assistantConfigurationRepository.findByAssistantId(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found for assistant: " + assistantId));

        configuration.setStatus(status);
        AssistantConfiguration updatedConfiguration = assistantConfigurationRepository.save(configuration);
        
        log.info("Configuration status updated successfully for assistant: {}", assistantId);
        return assistantConfigurationMapper.toDto(updatedConfiguration);
    }

    @Override
    public void deleteConfiguration(String assistantId) {
        log.info("Deleting configuration for assistant: {}", assistantId);
        
        AssistantConfiguration configuration = assistantConfigurationRepository.findByAssistantId(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found for assistant: " + assistantId));

        // Delete associated documents
        assistantDocumentService.deleteDocumentsByAssistantId(assistantId);
        
        // Delete configuration
        assistantConfigurationRepository.delete(configuration);
        
        log.info("Configuration deleted successfully for assistant: {}", assistantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAssistantId(String assistantId) {
        return assistantConfigurationRepository.findByAssistantId(assistantId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String assistantId, String clientId) {
        return assistantConfigurationRepository.findByAssistantIdAndClientId(assistantId, clientId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssistantConfigurationDTO> getConfigurationsByClientIdPaginated(String clientId, Pageable pageable, boolean includeDocuments) {
        log.debug("Fetching paginated configurations for client ID: {} with includeDocuments: {}", clientId, includeDocuments);

        Page<AssistantConfiguration> configurations = assistantConfigurationRepository.findByClientIdOrderByUpdatedAtDesc(clientId, pageable);
        return configurations.map(configuration -> {
            AssistantConfigurationDTO dto = assistantConfigurationMapper.toDto(configuration);
            if (includeDocuments) {
                dto.setDocuments(assistantDocumentService.getDocumentsByAssistantId(configuration.getAssistantId()));
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssistantConfigurationDTO> getConfigurationsByClientIdAndStatus(String clientId, AssistantConfiguration.Status status) {
        log.debug("Fetching configurations for client ID: {} with status: {}", clientId, status);

        List<AssistantConfiguration> configurations = assistantConfigurationRepository.findByClientIdAndStatusOrderByUpdatedAtDesc(clientId, status);
        return assistantConfigurationMapper.toDtoList(configurations);
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfigurationCountByClientId(String clientId) {
        log.debug("Fetching configuration count for client ID: {}", clientId);

        return assistantConfigurationRepository.countByClientId(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getConfigurationCountByClientIdAndStatus(String clientId) {
        log.debug("Fetching configuration count by status for client ID: {}", clientId);

        Map<String, Object> counts = new HashMap<>();
        counts.put("PENDING", assistantConfigurationRepository.countByClientIdAndStatus(clientId, AssistantConfiguration.Status.PENDING));
        counts.put("OPEN", assistantConfigurationRepository.countByClientIdAndStatus(clientId, AssistantConfiguration.Status.OPEN));
        counts.put("IN_PROGRESS", assistantConfigurationRepository.countByClientIdAndStatus(clientId, AssistantConfiguration.Status.IN_PROGRESS));
        counts.put("RESOLVED", assistantConfigurationRepository.countByClientIdAndStatus(clientId, AssistantConfiguration.Status.RESOLVED));
        counts.put("CLOSED", assistantConfigurationRepository.countByClientIdAndStatus(clientId, AssistantConfiguration.Status.CLOSED));
        
        return counts;
    }
} 