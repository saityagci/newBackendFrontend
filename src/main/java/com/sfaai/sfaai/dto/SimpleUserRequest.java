package com.sfaai.sfaai.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SimpleUserRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
} 