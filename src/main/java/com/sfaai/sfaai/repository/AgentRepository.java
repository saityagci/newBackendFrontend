package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByClientId(Long clientId);

}
