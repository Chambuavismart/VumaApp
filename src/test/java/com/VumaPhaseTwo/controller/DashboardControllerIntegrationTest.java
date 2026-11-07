package com.VumaPhaseTwo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/dashboard/overview returns 200 OK and expected structure")
    void testGetDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHistoricalMatches").isNumber())
                .andExpect(jsonPath("$.totalCanonicalTeams").isNumber())
                .andExpect(jsonPath("$.totalCompetitions").isNumber())
                .andExpect(jsonPath("$.totalSeasons").isNumber())
                .andExpect(jsonPath("$.totalCompetitionSeasons").isNumber())
                .andExpect(jsonPath("$.dataCoverage").isArray())
                .andExpect(jsonPath("$.recentImports").isArray());
    }
}
