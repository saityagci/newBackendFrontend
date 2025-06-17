package com.sfaai.sfaai.dto;

import com.sfaai.sfaai.dto.ClientDTO;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private ClientDTO user;
    private String token;
    private String redirectUrl;

    public LoginResponse(ClientDTO user, String token) {
        this.user = user;
        this.token = token;
        // Default redirect based on role
        this.redirectUrl = user.getRole().equalsIgnoreCase("ADMIN") ? "/AdminDashboard" : "/dashboard";
    }
}