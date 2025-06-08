package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.VoiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceLogRepository extends JpaRepository<VoiceLog, Long> {
}