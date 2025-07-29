package com.sfaai.sfaai.service;

import com.sfaai.sfaai.dto.AssistantDocumentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AssistantDocumentService {

    List<AssistantDocumentDTO> uploadDocuments(String assistantId, List<MultipartFile> files, String uploadedBy);

    List<AssistantDocumentDTO> getDocumentsByAssistantId(String assistantId);

    AssistantDocumentDTO getDocumentById(Long documentId);

    void deleteDocument(Long documentId);

    void deleteDocumentsByAssistantId(String assistantId);

    long getDocumentCountByAssistantId(String assistantId);

    boolean isValidFileType(String fileName);

    boolean isValidFileSize(long fileSize);

    String saveFile(MultipartFile file, String assistantId) throws IOException;
} 