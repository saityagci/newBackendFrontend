package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VapiAssistantRepository extends JpaRepository<VapiAssistant, String> {
    /**
     * Find all assistants with the given IDs
     * @param assistantIds List of assistant IDs
     * @return List of assistants
     */
    List<VapiAssistant> findByAssistantIdIn(List<String> assistantIds);

    /**
     * Find all assistants assigned to a specific client
     * @param client The client
     * @return List of assistants assigned to the client
     */
    List<VapiAssistant> findByClient(Client client);

    /**
     * Find all assistants assigned to a specific client ID
     * @param clientId The client ID
     * @return List of assistants assigned to the client
     */
    List<VapiAssistant> findByClientId(Long clientId);

    /**
     * Find assistants by status
     * @param status Status to filter by
     * @return List of assistants with the given status
     */
    List<VapiAssistant> findByStatus(String status);
}
