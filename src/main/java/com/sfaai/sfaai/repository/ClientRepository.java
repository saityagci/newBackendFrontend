package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.Client;
import com.sfaai.sfaai.entity.VapiAssistant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    /**
     * Find a client by API key
     * @param apiKey The API key
     * @return Optional client
     */
    Optional<Client> findByApiKey(String apiKey);

    /**
     * Find a client by email
     * @param email The client's email
     * @return Optional client
     */
    Optional<Client> findByEmail(String email);

    /**
     * Find a client by email ignoring case
     * @param email The client's email
     * @return Optional client
     */
    Optional<Client> findByEmailIgnoreCase(String email);

    /**
     * Find clients by role
     * @param role The role to search for
     * @return List of clients with the specified role
     */
    List<Client> findByRole(String role);

    /**
     * Find clients by role with pagination
     * @param role The role to search for
     * @param pageable Pagination information
     * @return Page of clients with the specified role
     */
    Page<Client> findByRole(String role, Pageable pageable);

    /**
     * Find clients whose full name contains the given text (case insensitive)
     * @param name The name fragment to search for
     * @return List of matching clients
     */
    List<Client> findByFullNameContainingIgnoreCase(String name);

    /**
     * Check if a client exists with the given email
     * @param email The email to check
     * @return true if a client with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a client exists with the given API key
     * @param apiKey The API key to check
     * @return true if a client with the API key exists, false otherwise
     */
    boolean existsByApiKey(String apiKey);

    /**
     * Find clients with the specified full name and email
     * @param fullName The full name to search for
     * @param email The email to search for
     * @return List of matching clients
     */
    @Query("SELECT c FROM Client c WHERE c.fullName = :fullName AND c.email = :email")
    List<Client> findByFullNameAndEmail(@Param("fullName") String fullName, @Param("email") String email);

    /**
     * Get client with their agents eagerly loaded
     * @param id The client ID
     * @return Optional client with agents loaded
     */
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.agents WHERE c.id = :id")
    Optional<Client> findByIdWithAgents(@Param("id") Long id);

    /**
     * Find all clients assigned to a specific Vapi assistant
     * @param vapiAssistantId The Vapi assistant ID
     * @return List of clients assigned to the assistant
     */
    List<Client> findByVapiAssistantId(String vapiAssistantId);

    /**
     * Find all clients that have a specific Vapi assistant ID in their vapiAssistants list
     * @param assistantId The Vapi assistant ID
     * @return List of clients with the assistant ID in their vapiAssistants
     */
    @Query("SELECT c FROM Client c JOIN c.vapiAssistants v WHERE v.assistantId = :assistantId")
    List<Client> findByVapiAssistantIdsContaining(@Param("assistantId") String assistantId);

    List<Client> findByVapiAssistantsContaining(VapiAssistant assistant);
}
