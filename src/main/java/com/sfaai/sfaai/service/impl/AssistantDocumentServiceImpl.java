package com.sfaai.sfaai.service.impl;

import com.sfaai.sfaai.dto.AssistantDocumentDTO;
import com.sfaai.sfaai.entity.AssistantDocument;
import com.sfaai.sfaai.mapper.AssistantDocumentMapper;
import com.sfaai.sfaai.repository.AssistantDocumentRepository;
import com.sfaai.sfaai.service.AssistantDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssistantDocumentServiceImpl implements AssistantDocumentService {

    private final AssistantDocumentRepository assistantDocumentRepository;
    private final AssistantDocumentMapper assistantDocumentMapper;

    @Value("${app.upload.directory:uploads/assistant-documents}")
    private String uploadDirectory;

    @Value("${app.max.file.size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.max.files.per.assistant:5}")
    private int maxFilesPerAssistant;

    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            ".pdf", ".doc", ".docx", ".txt"
    );

    @Override
    public List<AssistantDocumentDTO> uploadDocuments(String assistantId, List<MultipartFile> files, String uploadedBy) {
        log.info("Uploading {} documents for assistant: {}", files.size(), assistantId);

        // Check file count limit
        long currentFileCount = getDocumentCountByAssistantId(assistantId);
        if (currentFileCount + files.size() > maxFilesPerAssistant) {
            throw new IllegalArgumentException("Maximum " + maxFilesPerAssistant + " files allowed per assistant. Current: " + currentFileCount);
        }

        List<AssistantDocumentDTO> uploadedDocuments = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Validate file
                validateFile(file);

                // Save file to disk
                String filePath = saveFile(file, assistantId);

                // Create document record
                AssistantDocument document = AssistantDocument.builder()
                        .assistantId(assistantId)
                        .fileName(file.getOriginalFilename())
                        .filePath(filePath)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .uploadedBy(uploadedBy)
                        .build();

                AssistantDocument savedDocument = assistantDocumentRepository.save(document);
                uploadedDocuments.add(assistantDocumentMapper.toDto(savedDocument));

                log.info("Document uploaded successfully: {} for assistant: {}", file.getOriginalFilename(), assistantId);

            } catch (Exception e) {
                log.error("Failed to upload document: {} for assistant: {}", file.getOriginalFilename(), assistantId, e);
                throw new RuntimeException("Failed to upload document: " + file.getOriginalFilename(), e);
            }
        }

        return uploadedDocuments;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssistantDocumentDTO> getDocumentsByAssistantId(String assistantId) {
        log.info("Getting documents for assistant: {}", assistantId);
        
        List<AssistantDocument> documents = assistantDocumentRepository.findByAssistantId(assistantId);
        return assistantDocumentMapper.toDtoList(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public AssistantDocumentDTO getDocumentById(Long documentId) {
        log.info("Getting document by ID: {}", documentId);
        
        AssistantDocument document = assistantDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));
        
        return assistantDocumentMapper.toDto(document);
    }

    @Override
    public void deleteDocument(Long documentId) {
        log.info("Deleting document with ID: {}", documentId);
        
        AssistantDocument document = assistantDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        // Delete file from disk
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", document.getFilePath(), e);
        }

        // Delete from database
        assistantDocumentRepository.delete(document);
        
        log.info("Document deleted successfully with ID: {}", documentId);
    }

    @Override
    public void deleteDocumentsByAssistantId(String assistantId) {
        log.info("Deleting all documents for assistant: {}", assistantId);
        
        List<AssistantDocument> documents = assistantDocumentRepository.findByAssistantId(assistantId);
        
        for (AssistantDocument document : documents) {
            try {
                Path filePath = Paths.get(document.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Failed to delete file from disk: {}", document.getFilePath(), e);
            }
        }

        assistantDocumentRepository.deleteByAssistantId(assistantId);
        
        log.info("All documents deleted successfully for assistant: {}", assistantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDocumentCountByAssistantId(String assistantId) {
        return assistantDocumentRepository.countByAssistantId(assistantId);
    }

    @Override
    public boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return ALLOWED_FILE_EXTENSIONS.contains(extension);
    }

    @Override
    public boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSize;
    }

    @Override
    public String saveFile(MultipartFile file, String assistantId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory, assistantId);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!isValidFileSize(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        if (!isValidFileType(file.getOriginalFilename())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + ALLOWED_FILE_EXTENSIONS);
        }
    }
} 