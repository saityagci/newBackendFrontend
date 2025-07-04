package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimpleUserRequest {
    @JsonProperty("full_name")
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
} 