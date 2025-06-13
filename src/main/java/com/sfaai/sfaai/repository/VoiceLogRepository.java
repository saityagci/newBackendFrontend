package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.VoiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceLogRepository extends JpaRepository<VoiceLog, Long> {
    List<VoiceLog> findByAgentId(Long agentId);
    List<VoiceLog> findByClientId(Long clientId);
    @Query("SELECT v FROM VoiceLog v LEFT JOIN FETCH v.agent LEFT JOIN FETCH v.client WHERE v.agent.id = :agentId")
    List<VoiceLog> findByAgentIdWithJoins(@Param("agentId") Long agentId);
}