package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.ElevenLabsAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElevenLabsAssistantRepository extends JpaRepository<ElevenLabsAssistant, String> {

    /**
     * Find an assistant by name
     * @param name Assistant name
     * @return Optional containing the assistant if found
     */
    Optional<ElevenLabsAssistant> findByName(String name);

    /**
     * Find assistants by voice ID
     * @param voiceId Voice ID
     * @return List of assistants using the specified voice
     */
    List<ElevenLabsAssistant> findByVoiceId(String voiceId);

    /**
     * Find assistants by agent ID
     * @param agentId Agent ID
     * @return List of assistants assigned to the specified agent
     */
    List<ElevenLabsAssistant> findByAgentId(Long agentId);

    /**
     * Find assistants by client ID
     * @param clientId Client ID
     * @return List of assistants assigned to the specified client
     */
    List<ElevenLabsAssistant> findByClientId(Long clientId);
}
