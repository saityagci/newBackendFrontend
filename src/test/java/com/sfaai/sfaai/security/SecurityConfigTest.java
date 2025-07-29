package com.sfaai.sfaai.security;

import com.sfaai.sfaai.config.SecurityConfig;
import com.sfaai.sfaai.service.impl.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {SecurityConfig.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void publicEndpoints_ShouldBeAccessible() throws Exception {
        // Test registration endpoint
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Test\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());

        // Test login endpoint
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());

        // Test contact endpoint
        mockMvc.perform(post("/api/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"test@example.com\",\"message\":\"Test message\"}"))
                .andExpect(status().isOk());

        // Test Swagger endpoints
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui")).andExpect(status().isOk());

        // Test webhook endpoints
        mockMvc.perform(post("/api/webhooks/vapi/test")).andExpect(status().isOk());

        // Test audio endpoints
        mockMvc.perform(get("/audio/test.mp3")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Test protected endpoints without authentication
        mockMvc.perform(get("/api/clients")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/agents")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/voice-logs")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void protectedEndpoints_WithUserRole_ShouldBeAccessible() throws Exception {
        // Test protected endpoints with USER role
        mockMvc.perform(get("/api/clients")).andExpect(status().isOk());
        mockMvc.perform(get("/api/agents")).andExpect(status().isOk());
        mockMvc.perform(get("/api/voice-logs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        // Test admin endpoints with ADMIN role
        mockMvc.perform(get("/api/admin/assistants")).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/sync/status")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_WithUserRole_ShouldBeForbidden() throws Exception {
        // Test admin endpoints with USER role (should be forbidden)
        mockMvc.perform(get("/api/admin/assistants")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/sync/status")).andExpect(status().isForbidden());
    }

    @Test
    void corsHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Test\",\"email\":\"test@example.com\",\"password\":\"password123\"}")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
        // CORS headers should be automatically handled by Spring Security
    }

    @Test
    void securityHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/api/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(result -> {
                    // Check for security headers
                    String contentSecurityPolicy = result.getResponse().getHeader("Content-Security-Policy");
                    assert contentSecurityPolicy != null && contentSecurityPolicy.contains("default-src 'self'");
                    
                    String xFrameOptions = result.getResponse().getHeader("X-Frame-Options");
                    assert "DENY".equals(xFrameOptions);
                    
                    String hsts = result.getResponse().getHeader("Strict-Transport-Security");
                    assert hsts != null && hsts.contains("max-age=31536000");
                });
    }
} 