package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.ingestion.CsvValidationReport;
import com.VumaPhaseTwo.dto.ingestion.ImportGroupResponse;
import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FootballDataIngestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImportGroupRepository importGroupRepository;

    @Autowired
    private ImportBatchRepository importBatchRepository;

    @Autowired
    private HistoricalMatchRepository historicalMatchRepository;

    @Autowired
    private MatchSourceRecordRepository matchSourceRecordRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionAliasRepository competitionAliasRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private CompetitionSeasonRepository competitionSeasonRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamAliasRepository teamAliasRepository;

    private Competition premierLeague;
    private Season season2223;
    private Season season2324;
    private Team arsenal;
    private Team leicester;
    private Team nottmForest;

    @BeforeEach
    void setupCanonicalData() {
        premierLeague = competitionRepository.findByNormalizedNameAndCountry("premier league", "England").orElseGet(() ->
                competitionRepository.save(Competition.builder()
                        .name("Premier League")
                        .normalizedName("premier league")
                        .country("England")
                        .competitionType("LEAGUE")
                        .active(true)
                        .build())
        );

        competitionAliasRepository.save(CompetitionAlias.builder()
                .competition(premierLeague)
                .source("football data co uk")
                .sourceCode("E0")
                .rawAlias("E0")
                .normalizedAlias("e0")
                .countryContext("england")
                .active(true)
                .build());

        season2223 = seasonRepository.findBySeasonCode("2022-2023").orElseGet(() ->
                seasonRepository.save(Season.builder()
                        .seasonCode("2022-2023")
                        .seasonName("2022/2023")
                        .startDate(LocalDate.of(2022, 8, 1))
                        .endDate(LocalDate.of(2023, 5, 31))
                        .build())
        );

        season2324 = seasonRepository.findBySeasonCode("2023-2024").orElseGet(() ->
                seasonRepository.save(Season.builder()
                        .seasonCode("2023-2024")
                        .seasonName("2023/2024")
                        .startDate(LocalDate.of(2023, 8, 1))
                        .endDate(LocalDate.of(2024, 5, 31))
                        .build())
        );

        competitionSeasonRepository.findByCompetitionIdAndSeasonId(premierLeague.getId(), season2223.getId()).orElseGet(() ->
                competitionSeasonRepository.save(CompetitionSeason.builder()
                        .competition(premierLeague)
                        .season(season2223)
                        .startDate(LocalDate.of(2022, 8, 1))
                        .endDate(LocalDate.of(2023, 5, 31))
                        .active(true)
                        .build())
        );

        competitionSeasonRepository.findByCompetitionIdAndSeasonId(premierLeague.getId(), season2324.getId()).orElseGet(() ->
                competitionSeasonRepository.save(CompetitionSeason.builder()
                        .competition(premierLeague)
                        .season(season2324)
                        .startDate(LocalDate.of(2023, 8, 1))
                        .endDate(LocalDate.of(2024, 5, 31))
                        .active(true)
                        .build())
        );

        arsenal = teamRepository.findByNormalizedNameAndCountry("arsenal", "England").orElseGet(() ->
                teamRepository.save(Team.builder()
                        .name("Arsenal")
                        .normalizedName("arsenal")
                        .country("England")
                        .active(true)
                        .build())
        );

        teamAliasRepository.save(TeamAlias.builder()
                .team(arsenal)
                .source("football data co uk")
                .rawAlias("Arsenal")
                .normalizedAlias("arsenal")
                .countryContext("england")
                .active(true)
                .build());

        leicester = teamRepository.findByNormalizedNameAndCountry("leicester city", "England").orElseGet(() ->
                teamRepository.save(Team.builder()
                        .name("Leicester City")
                        .normalizedName("leicester city")
                        .country("England")
                        .active(true)
                        .build())
        );

        teamAliasRepository.save(TeamAlias.builder()
                .team(leicester)
                .source("football data co uk")
                .rawAlias("Leicester")
                .normalizedAlias("leicester")
                .countryContext("england")
                .active(true)
                .build());

        nottmForest = teamRepository.findByNormalizedNameAndCountry("nottingham forest", "England").orElseGet(() ->
                teamRepository.save(Team.builder()
                        .name("Nottingham Forest")
                        .normalizedName("nottingham forest")
                        .country("England")
                        .active(true)
                        .build())
        );

        teamAliasRepository.save(TeamAlias.builder()
                .team(nottmForest)
                .source("football data co uk")
                .rawAlias("Nott'm Forest")
                .normalizedAlias("nott'm forest")
                .countryContext("england")
                .active(true)
                .build());
    }

    @Test
    void testCatalogEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/ingestion/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].competitionCode").exists())
                .andExpect(jsonPath("$[0].downloadUrl").exists());
    }

    @Test
    void testValidateStackedCsvFile() throws Exception {
        String stackedCsv = """
                Season,Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR,HTHG,HTAG,HTR,HS,AS,HST,AST,Avg>2.5,Avg<2.5
                2022/2023,E0,13/08/2022,Arsenal,Leicester,4,2,H,2,0,H,19,6,7,2,1.85,1.95
                2023/2024,E0,12/08/2023,Arsenal,Nott'm Forest,2,1,H,2,0,H,15,6,7,2,1.60,2.30
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "E0_all.csv",
                "text/csv",
                stackedCsv.getBytes(StandardCharsets.UTF_8)
        );

        String responseJson = mockMvc.perform(multipart("/api/v1/ingestion/validate")
                        .file(file)
                        .param("competitionContext", "E0"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CsvValidationReport report = objectMapper.readValue(responseJson, CsvValidationReport.class);
        assertEquals("STACKED_MULTI_SEASON", report.getDistributionFormat());
        assertTrue(report.isValid());
        assertEquals(2, report.getSeasonSummaries().size());
        assertEquals(2, report.getTotalRows());
    }

    @Test
    void testImportStackedCsvSeparatesIntoDistinctBatches() throws Exception {
        String stackedCsv = """
                Season,Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR,HTHG,HTAG,HTR,HS,AS,HST,AST,Avg>2.5,Avg<2.5
                2022/2023,E0,13/08/2022,Arsenal,Leicester,4,2,H,2,0,H,19,6,7,2,1.85,1.95
                2023/2024,E0,12/08/2023,Arsenal,Nott'm Forest,2,1,H,2,0,H,15,6,7,2,1.60,2.30
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "E0_all.csv",
                "text/csv",
                stackedCsv.getBytes(StandardCharsets.UTF_8)
        );

        String responseJson = mockMvc.perform(multipart("/api/v1/ingestion/upload")
                        .file(file)
                        .param("competitionContext", "E0"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ImportGroupResponse groupResponse = objectMapper.readValue(responseJson, ImportGroupResponse.class);
        assertNotNull(groupResponse.getId());
        assertEquals("STACKED_MULTI_SEASON", groupResponse.getDistributionFormat());
        assertEquals(2, groupResponse.getBatches().size());

        // Verify each batch has exactly 1 distinct canonical season and competition_season
        var batch1 = groupResponse.getBatches().get(0);
        var batch2 = groupResponse.getBatches().get(1);

        assertNotNull(batch1.getSeasonId());
        assertNotNull(batch2.getSeasonId());
        assertNotEquals(batch1.getSeasonId(), batch2.getSeasonId());

        assertNotNull(batch1.getCompetitionSeasonId());
        assertNotNull(batch2.getCompetitionSeasonId());
        assertNotEquals(batch1.getCompetitionSeasonId(), batch2.getCompetitionSeasonId());

        assertEquals(1, batch1.getInsertedRows());
        assertEquals(1, batch2.getInsertedRows());
    }

    @Test
    void testReimportingSameCsvDoesNotCreateDuplicateMatches() throws Exception {
        String singleSeasonCsv = """
                Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR,HTHG,HTAG,HTR,HS,AS,HST,AST,Avg>2.5,Avg<2.5
                E0,12/08/2023,Arsenal,Nott'm Forest,2,1,H,2,0,H,15,6,7,2,1.60,2.30
                """;

        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "E0_2324.csv",
                "text/csv",
                singleSeasonCsv.getBytes(StandardCharsets.UTF_8)
        );

        // First import
        mockMvc.perform(multipart("/api/v1/ingestion/upload")
                        .file(file1)
                        .param("seasonContext", "2023/2024")
                        .param("competitionContext", "E0"))
                .andExpect(status().isOk());

        long countAfterFirst = historicalMatchRepository.count();

        // Second import of identical file
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "E0_2324.csv",
                "text/csv",
                singleSeasonCsv.getBytes(StandardCharsets.UTF_8)
        );

        String secondJson = mockMvc.perform(multipart("/api/v1/ingestion/upload")
                        .file(file2)
                        .param("seasonContext", "2023/2024")
                        .param("competitionContext", "E0"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ImportGroupResponse secondResponse = objectMapper.readValue(secondJson, ImportGroupResponse.class);
        assertEquals(1, secondResponse.getBatches().size());
        assertEquals(0, secondResponse.getBatches().get(0).getInsertedRows());
        assertEquals(1, secondResponse.getBatches().get(0).getSkippedRows());

        long countAfterSecond = historicalMatchRepository.count();
        assertEquals(countAfterFirst, countAfterSecond);
    }
}
