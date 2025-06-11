package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.VoiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceLogRepository extends JpaRepository<VoiceLog, Long> {
    List<VoiceLog> findByAgentId(Long agentId);
    List<VoiceLog> findByClientId(Long clientId);
    List<VoiceLog> findByAgentIdAndProvider(Long agentId, String provider);
}