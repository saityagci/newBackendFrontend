package com.sfaai.sfaai.mapper;

import com.sfaai.sfaai.dto.SupportRequestDTO;
import com.sfaai.sfaai.entity.SupportRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportRequestMapper {
    
    public SupportRequestDTO toDto(SupportRequest supportRequest) {
        if (supportRequest == null) {
            return null;
        }
        
        return SupportRequestDTO.builder()
                .id(supportRequest.getId())
                .userId(supportRequest.getUserId())
                .userEmail(supportRequest.getUserEmail())
                .userName(supportRequest.getUserName())
                .subject(supportRequest.getSubject())
                .message(supportRequest.getMessage())
                .status(supportRequest.getStatus())
                .createdAt(supportRequest.getCreatedAt())
                .updatedAt(supportRequest.getUpdatedAt())
                .build();
    }
    
    public SupportRequest toEntity(SupportRequestDTO supportRequestDTO) {
        if (supportRequestDTO == null) {
            return null;
        }
        
        return SupportRequest.builder()
                .userId(supportRequestDTO.getUserId())
                .userEmail(supportRequestDTO.getUserEmail())
                .userName(supportRequestDTO.getUserName())
                .subject(supportRequestDTO.getSubject())
                .message(supportRequestDTO.getMessage())
                .status(SupportRequest.Status.PENDING)
                .build();
    }
    
    public List<SupportRequestDTO> toDtoList(List<SupportRequest> supportRequests) {
        if (supportRequests == null) {
            return null;
        }
        
        return supportRequests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 