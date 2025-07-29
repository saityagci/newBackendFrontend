package com.sfaai.sfaai.repository;

import com.sfaai.sfaai.entity.AssistantDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantDocumentRepository extends JpaRepository<AssistantDocument, Long> {

    List<AssistantDocument> findByAssistantId(String assistantId);

    List<AssistantDocument> findByAssistantIdAndUploadedBy(String assistantId, String uploadedBy);

    @Query("SELECT COUNT(ad) FROM AssistantDocument ad WHERE ad.assistantId = :assistantId")
    long countByAssistantId(@Param("assistantId") String assistantId);

    void deleteByAssistantId(String assistantId);
} 