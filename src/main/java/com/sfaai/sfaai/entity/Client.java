package com.sfaai.sfaai.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Size(min = 2, max = 255)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(name = "api_key", nullable = false, unique = true, length = 36)
    private String apiKey;

    /**
     * Primary assistant assigned to the client (legacy support)
     */
    @Column(name = "vapi_assistant_id", length = 64)
    private String vapiAssistantId;

    /**
     * List of all assistants assigned to this client
     * A client can have multiple assistants assigned
     */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VapiAssistant> vapiAssistants = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Agent> agents = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VoiceLog> voiceLogs = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkflowLog> workflowLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add a VapiAssistant to this client
     * Maintains the bidirectional relationship
     * @param assistant The assistant to add
     */
    public void addVapiAssistant(VapiAssistant assistant) {
        if (!vapiAssistants.contains(assistant)) {
            vapiAssistants.add(assistant);
            assistant.setClient(this);

            // Update legacy vapiAssistantId if it's not set yet
            if (vapiAssistantId == null) {
                vapiAssistantId = assistant.getAssistantId();
            }
        }
    }

    /**
     * Helper method to remove a VapiAssistant from this client
     * Maintains the bidirectional relationship
     * @param assistant The assistant to remove
     */
    public void removeVapiAssistant(VapiAssistant assistant) {
        vapiAssistants.remove(assistant);
        assistant.setClient(null);

        // Update legacy vapiAssistantId if it matches the removed assistant
        if (vapiAssistantId != null && vapiAssistantId.equals(assistant.getAssistantId())) {
            vapiAssistantId = vapiAssistants.isEmpty() ? null : vapiAssistants.get(0).getAssistantId();
        }
    }

    /**
     * Helper method to remove all VapiAssistants from this client
     */
    public void clearVapiAssistants() {
        vapiAssistants.forEach(assistant -> assistant.setClient(null));
        vapiAssistants.clear();
        vapiAssistantId = null;
    }
}

