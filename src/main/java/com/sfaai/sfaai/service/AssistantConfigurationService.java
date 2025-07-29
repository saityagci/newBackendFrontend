package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AssistantConfigurationDTO;
import com.sfaai.sfaai.entity.AssistantConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AssistantConfigurationService {

    AssistantConfigurationDTO createConfiguration(String assistantId, String subject, String description, 
                                                 List<MultipartFile> documents, String clientId, String clientEmail, String clientName);

    AssistantConfigurationDTO getConfigurationByAssistantId(String assistantId);

    List<AssistantConfigurationDTO> getConfigurationsByClientId(String clientId);

    Page<AssistantConfigurationDTO> getConfigurationsByClientId(String clientId, Pageable pageable);

    List<AssistantConfigurationDTO> getConfigurationsByStatus(AssistantConfiguration.Status status);

    Page<AssistantConfigurationDTO> getConfigurationsByStatus(AssistantConfiguration.Status status, Pageable pageable);

    Page<AssistantConfigurationDTO> getAllConfigurations(Pageable pageable);

    Page<AssistantConfigurationDTO> getAllConfigurations(Pageable pageable, boolean includeDocuments);

    List<AssistantConfigurationDTO> getAllConfigurations();

    Page<AssistantConfigurationDTO> searchConfigurations(String subject, String clientId, 
                                                        AssistantConfiguration.Status status, String assistantId, Pageable pageable);

    Map<String, Object> getConfigurationCounts();

    AssistantConfigurationDTO updateConfigurationStatus(String assistantId, AssistantConfiguration.Status status);

    void deleteConfiguration(String assistantId);

    boolean existsByAssistantId(String assistantId);

    boolean hasPermission(String assistantId, String clientId);

    // User-specific methods for viewing their own configurations
    Page<AssistantConfigurationDTO> getConfigurationsByClientIdPaginated(String clientId, Pageable pageable, boolean includeDocuments);

    List<AssistantConfigurationDTO> getConfigurationsByClientIdAndStatus(String clientId, AssistantConfiguration.Status status);

    long getConfigurationCountByClientId(String clientId);

    Map<String, Object> getConfigurationCountByClientIdAndStatus(String clientId);
} 