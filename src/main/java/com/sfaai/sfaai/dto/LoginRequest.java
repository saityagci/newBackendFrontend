package com.sfaai.sfaai.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String email;
    private String password;
}