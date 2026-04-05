package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.AuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void login_withValidAdminCredentials_returnsToken() throws Exception {
        AuthRequest.Login login = new AuthRequest.Login();
        login.setUsername("admin");
        login.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
        AuthRequest.Login login = new AuthRequest.Login();
        login.setUsername("admin");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_withValidData_returnsCreated() throws Exception {
        AuthRequest.Register reg = new AuthRequest.Register();
        reg.setUsername("newuser_test");
        reg.setEmail("newuser_test@example.com");
        reg.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("VIEWER"));
    }

    @Test
    void register_withMissingFields_returnsBadRequest() throws Exception {
        AuthRequest.Register reg = new AuthRequest.Register();
        reg.setUsername("x"); // too short

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
