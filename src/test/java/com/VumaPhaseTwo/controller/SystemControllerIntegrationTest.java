package com.VumaPhaseTwo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SystemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/system/ping returns pong")
    void pingEndpointReturnsPong() throws Exception {
        mockMvc.perform(get("/api/v1/system/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    @DisplayName("GET /api/v1/system/status returns system status with database connectivity")
    void statusEndpointReturnsSystemStatus() throws Exception {
        mockMvc.perform(get("/api/v1/system/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationName", is("VumaPhaseTwo")))
                .andExpect(jsonPath("$.applicationVersion", notNullValue()))
                .andExpect(jsonPath("$.status", is("HEALTHY")))
                .andExpect(jsonPath("$.javaVersion", notNullValue()))
                .andExpect(jsonPath("$.database.status", is("ONLINE")))
                .andExpect(jsonPath("$.database.databaseName", equalToIgnoringCase("vumaapp")))
                .andExpect(jsonPath("$.database.databaseProductName", is("MySQL")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
