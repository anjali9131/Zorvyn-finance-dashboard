package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String viewerToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        viewerToken = getToken("viewer", "viewer123");
        adminToken  = getToken("admin",  "admin123");
    }

    private String getToken(String username, String password) throws Exception {
        AuthRequest.Login login = new AuthRequest.Login();
        login.setUsername(username);
        login.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    void getSummary_asViewer_returnsOk() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIncome").isNumber())
                .andExpect(jsonPath("$.data.totalExpenses").isNumber())
                .andExpect(jsonPath("$.data.netBalance").isNumber())
                .andExpect(jsonPath("$.data.recentActivity").isArray())
                .andExpect(jsonPath("$.data.monthlyTrends").isArray());
    }

    @Test
    void getSummary_asAdmin_returnsOk() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.incomeByCategory").isMap())
                .andExpect(jsonPath("$.data.expensesByCategory").isMap());
    }

    @Test
    void getSummary_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
