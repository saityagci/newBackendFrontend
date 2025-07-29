package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.SupportRequestCreateDTO;
import com.sfaai.sfaai.dto.SupportRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupportRequestService {
    
    /**
     * Create a new support request
     */
    SupportRequestDTO createSupportRequest(SupportRequestCreateDTO createDTO);
    
    /**
     * Get support request by ID
     */
    SupportRequestDTO getSupportRequestById(Long id);
    
    /**
     * Get all support requests with pagination
     */
    Page<SupportRequestDTO> getAllSupportRequests(Pageable pageable);
    
    /**
     * Get support requests by user ID
     */
    List<SupportRequestDTO> getSupportRequestsByUserId(String userId);
    
    /**
     * Get support requests by status
     */
    List<SupportRequestDTO> getSupportRequestsByStatus(String status);
    
    /**
     * Update support request status
     */
    SupportRequestDTO updateSupportRequestStatus(Long id, String status);
    
    /**
     * Delete support request
     */
    void deleteSupportRequest(Long id);

    /**
     * Get support requests by user ID with pagination
     */
    Page<SupportRequestDTO> getSupportRequestsByUserIdPaginated(String userId, Pageable pageable);

    /**
     * Get support request by ID and user ID (for user access control)
     */
    SupportRequestDTO getSupportRequestByIdAndUserId(Long id, String userId);

    /**
     * Get support requests by user ID and status
     */
    List<SupportRequestDTO> getSupportRequestsByUserIdAndStatus(String userId, String status);

    /**
     * Get support request count by user ID
     */
    long getSupportRequestCountByUserId(String userId);
} 