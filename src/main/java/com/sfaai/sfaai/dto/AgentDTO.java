package com.sfaai.sfaai.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AgentDTO {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Long clientId;

}
