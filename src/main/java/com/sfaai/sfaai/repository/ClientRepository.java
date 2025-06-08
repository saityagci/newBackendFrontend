package com.sfaai.sfaai.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.sfaai.sfaai.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByApiKey(String apiKey);
    Optional<Client> findByEmail(String email);


}
