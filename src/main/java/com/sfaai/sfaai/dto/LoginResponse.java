package com.sfaai.sfaai.dto;

import com.sfaai.sfaai.entity.Client;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Client user;
    private String token;
}