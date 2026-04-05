package com.finance.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.AuthRequest;
import com.finance.dashboard.dto.request.TransactionRequest;
import com.finance.dashboard.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String viewerToken;
    private String analystToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken  = getToken("admin",   "admin123");
        viewerToken = getToken("viewer",  "viewer123");
        analystToken = getToken("analyst","analyst123");
    }

    private String getToken(String username, String password) throws Exception {
        AuthRequest.Login login = new AuthRequest.Login();
        login.setUsername(username);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    @Test
    void getAll_asViewer_returnsOk() throws Exception {
        mockMvc.perform(get("/api/transactions")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getAll_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_asAdmin_returnsCreated() throws Exception {
        TransactionRequest req = buildRequest();

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.category").value("Test Category"))
                .andExpect(jsonPath("$.data.type").value("INCOME"));
    }

    @Test
    void create_asViewer_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withInvalidAmount_returnsBadRequest() throws Exception {
        TransactionRequest req = buildRequest();
        req.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_withTypeFilter_returnsFilteredResults() throws Exception {
        mockMvc.perform(get("/api/transactions?type=INCOME")
                .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void delete_asViewer_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/transactions/1")
                .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asAdmin_returnsOk() throws Exception {
        // First create one to delete
        MvcResult created = mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest())))
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/transactions/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private TransactionRequest buildRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("1500.00"));
        req.setType(TransactionType.INCOME);
        req.setCategory("Test Category");
        req.setDate(LocalDate.now());
        req.setNotes("Integration test transaction");
        return req;
    }
}
