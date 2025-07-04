package com.sfaai.sfaai.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ClientCreateDTO {
    @NotBlank(message = "Role is required")
    // Role defaults to USER if not specified
    private String role;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @AssertTrue(message = "You must agree to the terms and conditions")
    private boolean agree;

    // Custom validation for password matching
    @AssertTrue(message = "Passwords do not match")
    private boolean passwordMatching;

    // Method to validate password matching
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
