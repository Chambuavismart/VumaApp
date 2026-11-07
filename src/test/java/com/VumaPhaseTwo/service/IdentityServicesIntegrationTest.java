package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Identity Services Core Integration Tests")
class IdentityServicesIntegrationTest {

    @Autowired
    private TeamIdentityResolutionService teamResolutionService;

    @Autowired
    private CompetitionIdentityResolutionService competitionResolutionService;

    @Autowired
    private SeasonIdentityResolutionService seasonResolutionService;

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

    private Team arsenal;
    private Team liverpool;
    private Team sportingLisbon;
    private Team sportingGijon;
    private Competition premierLeague;
    private Competition championship;
    private Season season2425;
    private CompetitionSeason compSeasonPL2425;

    @BeforeEach
    void setUp() {
        arsenal = teamRepository.save(Team.builder()
                .name("Arsenal")
                .normalizedName("arsenal")
                .country("England")
                .active(true)
                .build());

        liverpool = teamRepository.save(Team.builder()
                .name("Liverpool")
                .normalizedName("liverpool")
                .country("England")
                .active(true)
                .build());

        // Same normalized name in different countries to test ambiguity
        sportingLisbon = teamRepository.save(Team.builder()
                .name("Sporting CP")
                .normalizedName("sporting")
                .country("Portugal")
                .active(true)
                .build());

        sportingGijon = teamRepository.save(Team.builder()
                .name("Sporting Gijon")
                .normalizedName("sporting")
                .country("Spain")
                .active(true)
                .build());

        premierLeague = competitionRepository.save(Competition.builder()
                .name("Premier League")
                .normalizedName("premier league")
                .country("England")
                .competitionType("LEAGUE")
                .active(true)
                .build());

        championship = competitionRepository.save(Competition.builder()
                .name("Championship")
                .normalizedName("championship")
                .country("England")
                .competitionType("LEAGUE")
                .active(true)
                .build());

        season2425 = seasonRepository.save(Season.builder()
                .seasonCode("2024/2025")
                .seasonName("2024/2025 Season")
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2025, 5, 31))
                .build());

        compSeasonPL2425 = competitionSeasonRepository.save(CompetitionSeason.builder()
                .competition(premierLeague)
                .season(season2425)
                .startDate(LocalDate.of(2024, 8, 15))
                .endDate(LocalDate.of(2025, 5, 25))
                .active(true)
                .build());

        teamAliasRepository.save(TeamAlias.builder()
                .team(arsenal)
                .source("football-data")
                .rawAlias("Arsenal")
                .normalizedAlias("arsenal")
                .countryContext("england")
                .active(true)
                .build());

        competitionAliasRepository.save(CompetitionAlias.builder()
                .competition(premierLeague)
                .source("football-data")
                .sourceCode("E0")
                .rawAlias("E0")
                .normalizedAlias("e0")
                .countryContext("england")
                .active(true)
                .build());

        competitionAliasRepository.save(CompetitionAlias.builder()
                .competition(championship)
                .source("football-data")
                .sourceCode("E1")
                .rawAlias("E1")
                .normalizedAlias("e1")
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
    @DisplayName("Team Resolution: Ambiguity when multiple canonical teams match without country context")
    void testAmbiguousTeamResolution() {
        TeamResolutionResult result = teamResolutionService.resolve(TeamResolutionRequest.builder()
                .incomingName("Sporting")
                .build());

        assertEquals(ResolutionStatus.AMBIGUOUS, result.getStatus());
        assertTrue(result.getCandidates().size() >= 2);

        // When country context is provided, it should resolve accurately
        TeamResolutionResult resultWithCountry = teamResolutionService.resolve(TeamResolutionRequest.builder()
                .incomingName("Sporting")
                .countryContext("Portugal")
                .build());

        assertEquals(ResolutionStatus.RESOLVED, resultWithCountry.getStatus());
        assertEquals(sportingLisbon.getId(), resultWithCountry.getResolvedTeamId());
    }

    @Test
    @DisplayName("Team Resolution: Whitespace, case, and punctuation handling")
    void testTeamResolutionVariations() {
        TeamResolutionResult result = teamResolutionService.resolve(TeamResolutionRequest.builder()
                .incomingName("  arsenal  f.c. ")
                .countryContext("England")
                .build());

        assertEquals(ResolutionStatus.RESOLVED, result.getStatus());
        assertEquals(arsenal.getId(), result.getResolvedTeamId());
    }

    @Test
    @DisplayName("Team Resolution: Invalid input returns INVALID_INPUT")
    void testInvalidTeamInput() {
        TeamResolutionResult result = teamResolutionService.resolve(TeamResolutionRequest.builder()
                .incomingName("   ")
                .build());

        assertEquals(ResolutionStatus.INVALID_INPUT, result.getStatus());
    }

    @Test
    @DisplayName("Competition Resolution: E0 and E1 resolve cleanly")
    void testCompetitionCodes() {
        CompetitionResolutionResult e0Result = competitionResolutionService.resolve(CompetitionResolutionRequest.builder()
                .incomingNameOrCode("E0")
                .provider("football-data")
                .countryContext("England")
                .build());

        assertEquals(ResolutionStatus.RESOLVED, e0Result.getStatus());
        assertEquals(premierLeague.getId(), e0Result.getResolvedCompetitionId());

        CompetitionResolutionResult e1Result = competitionResolutionService.resolve(CompetitionResolutionRequest.builder()
                .incomingNameOrCode("E1")
                .provider("football-data")
                .countryContext("England")
                .build());

        assertEquals(ResolutionStatus.RESOLVED, e1Result.getStatus());
        assertEquals(championship.getId(), e1Result.getResolvedCompetitionId());
    }

    @Test
    @DisplayName("Competition Resolution: Unknown competition returns UNRESOLVED")
    void testUnknownCompetition() {
        CompetitionResolutionResult result = competitionResolutionService.resolve(CompetitionResolutionRequest.builder()
                .incomingNameOrCode("UNKNOWN_CODE_99")
                .provider("football-data")
                .build());

        assertEquals(ResolutionStatus.UNRESOLVED, result.getStatus());
    }

    @Test
    @DisplayName("Season Resolution: Date inside competition season bounds resolves correctly")
    void testDateBasedSeasonResolution() {
        SeasonResolutionResult result = seasonResolutionService.resolve(SeasonResolutionRequest.builder()
                .competitionId(premierLeague.getId())
                .matchDate(LocalDate.of(2024, 11, 10))
                .build());

        assertEquals(ResolutionStatus.RESOLVED, result.getStatus());
        assertEquals(season2425.getId(), result.getResolvedSeasonId());
        assertEquals(compSeasonPL2425.getId(), result.getResolvedCompetitionSeasonId());
    }

    @Test
    @DisplayName("Season Resolution: Date outside competition season bounds returns UNRESOLVED")
    void testDateOutsideSeasonBounds() {
        SeasonResolutionResult result = seasonResolutionService.resolve(SeasonResolutionRequest.builder()
                .competitionId(premierLeague.getId())
                .matchDate(LocalDate.of(2026, 7, 10))
                .build());

        assertEquals(ResolutionStatus.UNRESOLVED, result.getStatus());
    }
}
