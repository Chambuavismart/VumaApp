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
class DatabaseInspectionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("19. Overview endpoint returns actual database metadata")
    void overviewEndpointReturnsActualDatabaseMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/database/overview").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.databaseName", equalToIgnoringCase("vumaapp")))
                .andExpect(jsonPath("$.databaseEngine", is("MySQL")))
                .andExpect(jsonPath("$.connectionStatus", is("ONLINE")))
                .andExpect(jsonPath("$.totalTables", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.totalForeignKeys", greaterThan(0)))
                .andExpect(jsonPath("$.flywayStatus", notNullValue()))
                .andExpect(jsonPath("$.characterSet", notNullValue()));
    }

    @Test
    @DisplayName("20. Table endpoint returns actual table information")
    void tablesEndpointReturnsTableList() throws Exception {
        mockMvc.perform(get("/api/v1/database/tables").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[*].tableName", hasItems(
                        "competitions",
                        "competition_aliases",
                        "seasons",
                        "competition_seasons",
                        "teams",
                        "team_aliases",
                        "historical_matches",
                        "import_batches",
                        "import_quarantine_records",
                        "match_source_records"
                )));
    }

    @Test
    @DisplayName("20b. Table structure endpoint returns column and index details")
    void tableStructureEndpointReturnsDetails() throws Exception {
        mockMvc.perform(get("/api/v1/database/tables/historical_matches").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableName", is("historical_matches")))
                .andExpect(jsonPath("$.columns", hasSize(greaterThan(20))))
                .andExpect(jsonPath("$.indexes", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.columns[?(@.columnName=='home_team_id')].foreignKey", hasItem(true)))
                .andExpect(jsonPath("$.columns[?(@.columnName=='away_team_id')].foreignKey", hasItem(true)));
    }

    @Test
    @DisplayName("21. Invalid table names are safely rejected")
    void invalidTableNamesAreSafelyRejected() throws Exception {
        // Non-existent table returns 404
        mockMvc.perform(get("/api/v1/database/tables/invalid_table_non_existent").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Invalid regex character returns 400
        mockMvc.perform(get("/api/v1/database/tables/invalid*name").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("22. Relationship endpoint returns actual foreign keys")
    void relationshipsEndpointReturnsForeignKeys() throws Exception {
        mockMvc.perform(get("/api/v1/database/relationships").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[?(@.sourceTable=='historical_matches' && @.sourceColumn=='competition_id')].referencedTable", hasItem("competitions")))
                .andExpect(jsonPath("$[?(@.sourceTable=='historical_matches' && @.sourceColumn=='home_team_id')].referencedTable", hasItem("teams")))
                .andExpect(jsonPath("$[?(@.sourceTable=='match_source_records' && @.sourceColumn=='historical_match_id')].referencedTable", hasItem("historical_matches")));
    }

    @Test
    @DisplayName("23. Migration endpoint returns actual Flyway records")
    void migrationsEndpointReturnsFlywayRecords() throws Exception {
        mockMvc.perform(get("/api/v1/database/migrations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$[0].version", is("1")))
                .andExpect(jsonPath("$[0].success", is(true)))
                .andExpect(jsonPath("$[6].version", is("7")))
                .andExpect(jsonPath("$[6].success", is(true)))
                .andExpect(jsonPath("$[7].version", is("8")))
                .andExpect(jsonPath("$[7].success", is(true)));
    }
}
