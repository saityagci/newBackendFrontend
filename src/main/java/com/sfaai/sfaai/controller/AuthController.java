package com.sfaai.sfaai.controller;

import com.sfaai.sfaai.dto.LoginRequest;
import com.sfaai.sfaai.dto.LoginResponse;
import com.sfaai.sfaai.dto.RegisterRequest;
import com.sfaai.sfaai.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Optional: for testing from frontend
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Handle validation exceptions
     * @param ex The exception
     * @return Bad request response with error details
     */
    @ExceptionHandler({IllegalArgumentException.class, org.springframework.security.authentication.BadCredentialsException.class})
    public ResponseEntity<String> handleValidationExceptions(Exception ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // Normalize the email if provided
            if (request.getEmail() != null) {
                request.setEmail(request.getEmail().trim());
            }

            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            // Don't expose internal details in the exception message
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
    }
}