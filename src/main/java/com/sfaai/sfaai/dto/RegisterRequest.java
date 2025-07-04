package com.sfaai.sfaai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @JsonProperty("full_name")
    private String fullName;
    private String email;
    private String password;
    @JsonProperty("confirm_password")
    private String confirmPassword;
    private boolean agree;
}
