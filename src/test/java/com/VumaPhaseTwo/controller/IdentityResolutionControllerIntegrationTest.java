package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Identity Resolution Controller Integration Tests")
class IdentityResolutionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamAliasRepository teamAliasRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionAliasRepository competitionAliasRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private CompetitionSeasonRepository competitionSeasonRepository;

    private Team testTeamArsenal;
    private Team testTeamLiverpool;
    private Competition testCompetitionPL;
    private Competition testCompetitionLiga;
    private Season testSeason2425;
    private CompetitionSeason testCompSeasonPL2425;

    @BeforeEach
    void setUp() {
        // Create canonical test records
        testTeamArsenal = teamRepository.save(Team.builder()
                .name("Arsenal")
                .normalizedName("arsenal")
                .country("England")
                .active(true)
                .build());

        testTeamLiverpool = teamRepository.save(Team.builder()
                .name("Liverpool")
                .normalizedName("liverpool")
                .country("England")
                .active(true)
                .build());

        testCompetitionPL = competitionRepository.save(Competition.builder()
                .name("Premier League")
                .normalizedName("premier league")
                .country("England")
                .competitionType("LEAGUE")
                .active(true)
                .build());

        testCompetitionLiga = competitionRepository.save(Competition.builder()
                .name("La Liga")
                .normalizedName("la liga")
                .country("Spain")
                .competitionType("LEAGUE")
                .active(true)
                .build());

        testSeason2425 = seasonRepository.save(Season.builder()
                .seasonCode("2024/2025")
                .seasonName("2024/2025 Season")
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2025, 5, 31))
                .build());

        testCompSeasonPL2425 = competitionSeasonRepository.save(CompetitionSeason.builder()
                .competition(testCompetitionPL)
                .season(testSeason2425)
                .startDate(LocalDate.of(2024, 8, 10))
                .endDate(LocalDate.of(2025, 5, 25))
                .active(true)
                .build());

        // Register Aliases
        teamAliasRepository.save(TeamAlias.builder()
                .team(testTeamArsenal)
                .source("football-data")
                .rawAlias("Arsenal FC")
                .normalizedAlias("arsenal fc")
                .countryContext("england")
                .competitionContext("premier league")
                .active(true)
                .build());

        competitionAliasRepository.save(CompetitionAlias.builder()
                .competition(testCompetitionPL)
                .source("football-data")
                .sourceCode("E0")
                .rawAlias("E0")
                .normalizedAlias("e0")
                .countryContext("england")
                .active(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        teamAliasRepository.deleteAll();
        competitionAliasRepository.deleteAll();
        competitionSeasonRepository.deleteAll();
        seasonRepository.deleteAll();
        teamRepository.deleteAll();
        competitionRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/identity/overview should return live database counts")
    void testGetOverview() throws Exception {
        mockMvc.perform(get("/api/v1/identity/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCanonicalTeams", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalCanonicalCompetitions", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalSeasons", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalCompetitionSeasons", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalTeamAliases", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalCompetitionAliases", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/v1/identity/teams/resolve - should resolve team via exact alias")
    void testResolveTeamViaAlias() throws Exception {
        TeamResolutionRequest request = TeamResolutionRequest.builder()
                .incomingName("Arsenal FC")
                .provider("football-data")
                .countryContext("England")
                .competitionContext("Premier League")
                .build();

        mockMvc.perform(post("/api/v1/identity/teams/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedTeamId", is(testTeamArsenal.getId().intValue())))
                .andExpect(jsonPath("$.resolvedTeamName", is("Arsenal")));
    }

    @Test
    @DisplayName("POST /api/v1/identity/teams/resolve - should resolve canonical team directly")
    void testResolveTeamCanonicalDirect() throws Exception {
        TeamResolutionRequest request = TeamResolutionRequest.builder()
                .incomingName("Liverpool")
                .countryContext("England")
                .build();

        mockMvc.perform(post("/api/v1/identity/teams/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedTeamId", is(testTeamLiverpool.getId().intValue())))
                .andExpect(jsonPath("$.resolvedTeamName", is("Liverpool")));
    }

    @Test
    @DisplayName("POST /api/v1/identity/teams/resolve - should return UNRESOLVED for unknown team")
    void testResolveTeamUnresolved() throws Exception {
        TeamResolutionRequest request = TeamResolutionRequest.builder()
                .incomingName("Fictional Nonexistent FC")
                .provider("football-data")
                .build();

        mockMvc.perform(post("/api/v1/identity/teams/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNRESOLVED")));
    }

    @Test
    @DisplayName("POST /api/v1/identity/competitions/resolve - should resolve Football-Data E0 code")
    void testResolveCompetitionE0() throws Exception {
        CompetitionResolutionRequest request = CompetitionResolutionRequest.builder()
                .incomingNameOrCode("E0")
                .provider("football-data")
                .countryContext("England")
                .build();

        mockMvc.perform(post("/api/v1/identity/competitions/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedCompetitionId", is(testCompetitionPL.getId().intValue())))
                .andExpect(jsonPath("$.resolvedCompetitionName", is("Premier League")));
    }

    @Test
    @DisplayName("POST /api/v1/identity/seasons/resolve - should resolve cross-year season and date")
    void testResolveSeason() throws Exception {
        SeasonResolutionRequest request = SeasonResolutionRequest.builder()
                .incomingSeasonRepresentation("2024/25")
                .competitionId(testCompetitionPL.getId())
                .build();

        mockMvc.perform(post("/api/v1/identity/seasons/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolvedSeasonCode", is("2024/2025")))
                .andExpect(jsonPath("$.resolvedCompetitionSeasonId", is(testCompSeasonPL2425.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/v1/identity/teams/{teamId}/aliases - should register alias and prevent conflicting mapping")
    void testRegisterTeamAliasAndConflict() throws Exception {
        RegisterTeamAliasRequest request = RegisterTeamAliasRequest.builder()
                .source("provider-x")
                .rawAlias("The Gunners")
                .countryContext("England")
                .build();

        // 1. Register alias against Arsenal
        mockMvc.perform(post("/api/v1/identity/teams/" + testTeamArsenal.getId() + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId", is(testTeamArsenal.getId().intValue())))
                .andExpect(jsonPath("$.normalizedAlias", is("the gunners")));

        // 2. Register same alias against Liverpool -> CONFLICT 409
        mockMvc.perform(post("/api/v1/identity/teams/" + testTeamLiverpool.getId() + "/aliases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/v1/identity/teams - should list teams with pagination and search")
    void testListTeams() throws Exception {
        mockMvc.perform(get("/api/v1/identity/teams")
                        .param("query", "Arsenal")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Arsenal")));
    }
}
