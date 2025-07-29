package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.SupportRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    
    /**
     * Find all support requests by user ID
     */
    List<SupportRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find all support requests by user email
     */
    List<SupportRequest> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    /**
     * Find all support requests by status
     */
    List<SupportRequest> findByStatusOrderByCreatedAtDesc(SupportRequest.Status status);
    
    /**
     * Find all support requests by user ID with pagination
     */
    Page<SupportRequest> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find all support requests by status with pagination
     */
    Page<SupportRequest> findByStatus(SupportRequest.Status status, Pageable pageable);
    
    /**
     * Count support requests by status
     */
    long countByStatus(SupportRequest.Status status);
    
    /**
     * Find recent support requests (last 30 days)
     */
    @Query("SELECT sr FROM SupportRequest sr WHERE sr.createdAt >= :startDate ORDER BY sr.createdAt DESC")
    List<SupportRequest> findRecentRequests(@Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Find all support requests by user ID with pagination (ordered by created date desc)
     */
    Page<SupportRequest> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find support request by ID and user ID (for user access control)
     */
    @Query("SELECT sr FROM SupportRequest sr WHERE sr.id = :id AND sr.userId = :userId")
    java.util.Optional<SupportRequest> findByIdAndUserId(@Param("id") Long id, @Param("userId") String userId);

    /**
     * Find all support requests by user ID and status (ordered by created date desc)
     */
    List<SupportRequest> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, SupportRequest.Status status);

    /**
     * Count support requests by user ID
     */
    long countByUserId(String userId);
} 