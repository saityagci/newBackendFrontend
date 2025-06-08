package com.sfaai.sfaai.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class VoiceLogDTO {
    private Long id;
    private String phoneNumber;
    private String transcript;
    private LocalDateTime callTime;
    private String source;
}