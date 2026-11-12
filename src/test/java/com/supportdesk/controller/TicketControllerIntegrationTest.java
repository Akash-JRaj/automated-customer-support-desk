package com.supportdesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.dto.AuthRequest;
import com.supportdesk.dto.EmailIngestionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/tickets - should return paginated tickets")
    void testGetTickets() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[0].ticketNumber", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/metrics - should return metrics")
    void testGetDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets", greaterThan(0)))
                .andExpect(jsonPath("$.resolutionRate", notNullValue()))
                .andExpect(jsonPath("$.aiAutonomyRate", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/emails/ingest - should ingest email and return ticket")
    void testIngestEmailEndpoint() throws Exception {
        EmailIngestionRequest request = new EmailIngestionRequest(
                "developer@startup.com",
                "Alice Dev",
                "API endpoint timeout issue with 500 status",
                "We are seeing connection timeouts when hitting the billing endpoint."
        );

        mockMvc.perform(post("/api/v1/emails/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketNumber", startsWith("TICK-")))
                .andExpect(jsonPath("$.customerEmail", is("developer@startup.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - should authenticate admin and return JWT token")
    void testLoginSuccess() throws Exception {
        AuthRequest authRequest = new AuthRequest("admin@supportdesk.io", "Admin@123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.email", is("admin@supportdesk.io")));
    }
}
