package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to Agent (Voice Agent)
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;

    // Link to Client (optional, can be accessed via agent as well)
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // Link to VoiceLog (optional, for deep traceability)
    @ManyToOne
    @JoinColumn(name = "voice_log_id")
    private VoiceLog voiceLog;

    private String workflowName;

    @Lob
    @Column(columnDefinition = "text")
    private String inputData;

    @Lob
    @Column(columnDefinition = "text")
    private String outputData;

    private LocalDateTime createdAt;
}