package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.SupportRequestCreateDTO;
import com.sfaai.sfaai.dto.SupportRequestDTO;
import com.sfaai.sfaai.entity.SupportRequest;
import com.sfaai.sfaai.exception.ResourceNotFoundException;
import com.sfaai.sfaai.mapper.SupportRequestMapper;
import com.sfaai.sfaai.repository.SupportRequestRepository;
import com.sfaai.sfaai.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupportRequestServiceImpl implements SupportRequestService {

    private final SupportRequestRepository supportRequestRepository;
    private final SupportRequestMapper supportRequestMapper;

    @Override
    public SupportRequestDTO createSupportRequest(SupportRequestCreateDTO createDTO) {
        log.info("Creating support request for user: {}", createDTO.getUserEmail());
        
        // Validate required fields
        if (createDTO.getSubject() == null || createDTO.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        
        if (createDTO.getMessage() == null || createDTO.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        
        if (createDTO.getUserId() == null || createDTO.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (createDTO.getUserEmail() == null || createDTO.getUserEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required");
        }

        // Create the support request entity
        SupportRequest supportRequest = SupportRequest.builder()
                .userId(createDTO.getUserId().trim())
                .userEmail(createDTO.getUserEmail().trim().toLowerCase())
                .userName(createDTO.getUserName() != null ? createDTO.getUserName().trim() : null)
                .subject(createDTO.getSubject().trim())
                .message(createDTO.getMessage().trim())
                .status(SupportRequest.Status.PENDING)
                .build();

        // Save to database
        SupportRequest savedRequest = supportRequestRepository.save(supportRequest);
        
        log.info("Support request created successfully with ID: {}", savedRequest.getId());
        
        // TODO: Send email notification to support team (optional)
        // sendSupportNotification(savedRequest);
        
        return supportRequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportRequestDTO getSupportRequestById(Long id) {
        log.debug("Fetching support request by ID: {}", id);
        
        SupportRequest supportRequest = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support request not found with id: " + id));
        
        return supportRequestMapper.toDto(supportRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportRequestDTO> getAllSupportRequests(Pageable pageable) {
        log.debug("Fetching all support requests with pagination: {}", pageable);
        
        Page<SupportRequest> supportRequests = supportRequestRepository.findAll(pageable);
        return supportRequests.map(supportRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestDTO> getSupportRequestsByUserId(String userId) {
        log.debug("Fetching support requests for user ID: {}", userId);
        
        List<SupportRequest> supportRequests = supportRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return supportRequestMapper.toDtoList(supportRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestDTO> getSupportRequestsByStatus(String status) {
        log.debug("Fetching support requests by status: {}", status);
        
        try {
            SupportRequest.Status requestStatus = SupportRequest.Status.valueOf(status.toUpperCase());
            List<SupportRequest> supportRequests = supportRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus);
            return supportRequestMapper.toDtoList(supportRequests);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    public SupportRequestDTO updateSupportRequestStatus(Long id, String status) {
        log.info("Updating support request status - ID: {}, Status: {}", id, status);
        
        SupportRequest supportRequest = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support request not found with id: " + id));
        
        try {
            SupportRequest.Status newStatus = SupportRequest.Status.valueOf(status.toUpperCase());
            supportRequest.setStatus(newStatus);
            
            SupportRequest updatedRequest = supportRequestRepository.save(supportRequest);
            log.info("Support request status updated successfully - ID: {}, New Status: {}", id, newStatus);
            
            return supportRequestMapper.toDto(updatedRequest);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    public void deleteSupportRequest(Long id) {
        log.info("Deleting support request with ID: {}", id);
        
        if (!supportRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Support request not found with id: " + id);
        }
        
        supportRequestRepository.deleteById(id);
        log.info("Support request deleted successfully - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportRequestDTO> getSupportRequestsByUserIdPaginated(String userId, Pageable pageable) {
        log.debug("Fetching paginated support requests for user ID: {}", userId);
        
        Page<SupportRequest> supportRequests = supportRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return supportRequests.map(supportRequestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportRequestDTO getSupportRequestByIdAndUserId(Long id, String userId) {
        log.debug("Fetching support request by ID: {} for user ID: {}", id, userId);
        
        SupportRequest supportRequest = supportRequestRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Support request not found or access denied"));
        
        return supportRequestMapper.toDto(supportRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestDTO> getSupportRequestsByUserIdAndStatus(String userId, String status) {
        log.debug("Fetching support requests for user ID: {} with status: {}", userId, status);
        
        try {
            SupportRequest.Status requestStatus = SupportRequest.Status.valueOf(status.toUpperCase());
            List<SupportRequest> supportRequests = supportRequestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, requestStatus);
            return supportRequestMapper.toDtoList(supportRequests);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getSupportRequestCountByUserId(String userId) {
        log.debug("Fetching support request count for user ID: {}", userId);
        
        return supportRequestRepository.countByUserId(userId);
    }
} 