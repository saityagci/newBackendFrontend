package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.AssistantDocumentDTO;
import com.sfaai.sfaai.entity.AssistantDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssistantDocumentMapper {

    public AssistantDocumentDTO toDto(AssistantDocument assistantDocument) {
        if (assistantDocument == null) {
            return null;
        }

        return AssistantDocumentDTO.builder()
                .id(assistantDocument.getId())
                .assistantId(assistantDocument.getAssistantId())
                .fileName(assistantDocument.getFileName())
                .filePath(assistantDocument.getFilePath())
                .fileType(assistantDocument.getFileType())
                .fileSize(assistantDocument.getFileSize())
                .uploadedBy(assistantDocument.getUploadedBy())
                .uploadedAt(assistantDocument.getUploadedAt())
                .build();
    }

    public AssistantDocument toEntity(AssistantDocumentDTO assistantDocumentDTO) {
        if (assistantDocumentDTO == null) {
            return null;
        }

        return AssistantDocument.builder()
                .assistantId(assistantDocumentDTO.getAssistantId())
                .fileName(assistantDocumentDTO.getFileName())
                .filePath(assistantDocumentDTO.getFilePath())
                .fileType(assistantDocumentDTO.getFileType())
                .fileSize(assistantDocumentDTO.getFileSize())
                .uploadedBy(assistantDocumentDTO.getUploadedBy())
                .build();
    }

    public List<AssistantDocumentDTO> toDtoList(List<AssistantDocument> assistantDocuments) {
        if (assistantDocuments == null) {
            return null;
        }

        return assistantDocuments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 