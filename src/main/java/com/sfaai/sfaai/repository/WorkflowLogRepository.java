package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.dto.WorkflowLogDTO;
import com.sfaai.sfaai.entity.WorkflowLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowLogRepository extends JpaRepository<WorkflowLog, Long> {
    Optional<WorkflowLogDTO> findByClientId(Long clientId);
}