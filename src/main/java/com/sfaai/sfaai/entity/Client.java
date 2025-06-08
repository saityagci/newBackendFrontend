package com.sfaai.sfaai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role")
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    @Column(unique = true, nullable = false)
    private String apiKey;
    @PrePersist
    public void generateApiKey() {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = UUID.randomUUID().toString();
        }
    }
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowLog> logs;


}
