package com.sfaai.sfaai.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;
    private boolean agree;
    private String apiKey;
}
