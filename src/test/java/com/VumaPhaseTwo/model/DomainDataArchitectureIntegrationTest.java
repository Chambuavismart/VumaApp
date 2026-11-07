package com.VumaPhaseTwo.model;

import com.VumaPhaseTwo.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DomainDataArchitectureIntegrationTest {

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
    @Autowired
    private HistoricalMatchRepository historicalMatchRepository;
    @Autowired
    private ImportBatchRepository importBatchRepository;
    @Autowired
    private ImportQuarantineRecordRepository importQuarantineRecordRepository;
    @Autowired
    private MatchSourceRecordRepository matchSourceRecordRepository;

    @Test
    @DisplayName("5. Different teams can have similar names in different countries")
    void differentTeamsCanHaveSimilarNamesInDifferentCountries() {
        Team arsenalUk = teamRepository.save(Team.builder()
                .name("Arsenal FC")
                .normalizedName("arsenal")
                .country("England")
                .build());

        Team arsenalArg = teamRepository.save(Team.builder()
                .name("Arsenal de Sarandí")
                .normalizedName("arsenal")
                .country("Argentina")
                .build());

        assertThat(arsenalUk.getId()).isNotNull();
        assertThat(arsenalArg.getId()).isNotNull();
        assertThat(arsenalUk.getId()).isNotEqualTo(arsenalArg.getId());
        assertThat(arsenalUk.getCountry()).isEqualTo("England");
        assertThat(arsenalArg.getCountry()).isEqualTo("Argentina");
    }

    @Test
    @DisplayName("6. Team aliases can exist in different valid contexts")
    void teamAliasesCanExistInDifferentContexts() {
        Team team = teamRepository.save(Team.builder()
                .name("Manchester United")
                .normalizedName("manchester united")
                .country("England")
                .build());

        TeamAlias alias1 = teamAliasRepository.save(TeamAlias.builder()
                .team(team)
                .source("football-data")
                .rawAlias("Man United")
                .normalizedAlias("man united")
                .countryContext("England")
                .competitionContext("Premier League")
                .build());

        TeamAlias alias2 = teamAliasRepository.save(TeamAlias.builder()
                .team(team)
                .source("api-football")
                .rawAlias("Manchester Utd")
                .normalizedAlias("manchester utd")
                .countryContext("England")
                .competitionContext("Premier League")
                .build());

        assertThat(alias1.getId()).isNotNull();
        assertThat(alias2.getId()).isNotNull();
        List<TeamAlias> aliases = teamAliasRepository.findByTeamId(team.getId());
        assertThat(aliases).hasSize(2);
    }

    @Test
    @DisplayName("7. Competition aliases support source-specific codes")
    void competitionAliasesSupportSourceSpecificCodes() {
        Competition comp = competitionRepository.save(Competition.builder()
                .name("Premier League")
                .normalizedName("premier league")
                .country("England")
                .competitionType("LEAGUE")
                .build());

        CompetitionAlias alias = competitionAliasRepository.save(CompetitionAlias.builder()
                .competition(comp)
                .source("football-data")
                .sourceCode("E0")
                .rawAlias("Premier League")
                .normalizedAlias("premier league")
                .countryContext("England")
                .build());

        assertThat(alias.getSourceCode()).isEqualTo("E0");
        assertThat(alias.getCompetition().getId()).isEqualTo(comp.getId());
    }

    @Test
    @DisplayName("8. Competition-season associations are valid")
    void competitionSeasonAssociationsAreValid() {
        Competition comp = competitionRepository.save(Competition.builder()
                .name("La Liga")
                .normalizedName("la liga")
                .country("Spain")
                .build());

        Season season = seasonRepository.save(Season.builder()
                .seasonCode("2025/2026")
                .seasonName("2025/2026 Season")
                .startDate(LocalDate.of(2025, 8, 1))
                .endDate(LocalDate.of(2026, 5, 31))
                .build());

        CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                .competition(comp)
                .season(season)
                .startDate(LocalDate.of(2025, 8, 15))
                .endDate(LocalDate.of(2026, 5, 24))
                .active(true)
                .build());

        assertThat(cs.getId()).isNotNull();
        Optional<CompetitionSeason> found = competitionSeasonRepository.findByCompetitionIdAndSeasonId(comp.getId(), season.getId());
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("9. Valid home and away team relationships are accepted")
    void validHomeAndAwayTeamRelationshipsAreAccepted() {
        Competition comp = competitionRepository.save(Competition.builder()
                .name("Premier League")
                .normalizedName("premier league")
                .country("England")
                .build());

        Season season = seasonRepository.save(Season.builder()
                .seasonCode("2024/2025")
                .seasonName("2024/2025 Season")
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2025, 5, 31))
                .build());

        CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                .competition(comp)
                .season(season)
                .startDate(LocalDate.of(2024, 8, 15))
                .endDate(LocalDate.of(2025, 5, 25))
                .build());

        Team home = teamRepository.save(Team.builder().name("Liverpool").normalizedName("liverpool").country("England").build());
        Team away = teamRepository.save(Team.builder().name("Chelsea").normalizedName("chelsea").country("England").build());

        HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                .competition(comp)
                .season(season)
                .competitionSeason(cs)
                .homeTeam(home)
                .awayTeam(away)
                .matchDate(LocalDate.of(2024, 10, 20))
                .kickoffTime(LocalTime.of(16, 30))
                .matchStatus("FINISHED")
                .homeGoals(2)
                .awayGoals(1)
                .fullTimeResult("H")
                .halfTimeHomeGoals(1)
                .halfTimeAwayGoals(0)
                .halfTimeResult("H")
                .homeShots(15)
                .awayShots(10)
                .homeShotsOnTarget(6)
                .awayShotsOnTarget(3)
                .homeWinOdds(new BigDecimal("1.6500"))
                .drawOdds(new BigDecimal("4.2000"))
                .awayWinOdds(new BigDecimal("5.0000"))
                .naturalMatchKey("ENG-PL-20242025-LIV-CHE-20241020")
                .build());

        assertThat(match.getId()).isNotNull();
        assertThat(match.getHomeTeam().getId()).isEqualTo(home.getId());
        assertThat(match.getAwayTeam().getId()).isEqualTo(away.getId());
    }

    @Test
    @DisplayName("11 & 12. Nullable statistics are accepted and genuine zero is distinguished from NULL")
    void nullableStatisticsDistinguishedFromZero() {
        Competition comp = competitionRepository.save(Competition.builder().name("Serie A").normalizedName("serie a").country("Italy").build());
        Season season = seasonRepository.save(Season.builder().seasonCode("2023/2024").seasonName("2023/2024").startDate(LocalDate.of(2023, 8, 1)).endDate(LocalDate.of(2024, 5, 31)).build());
        CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2023, 8, 19)).endDate(LocalDate.of(2024, 5, 26)).build());
        Team home = teamRepository.save(Team.builder().name("Juventus").normalizedName("juventus").country("Italy").build());
        Team away = teamRepository.save(Team.builder().name("Inter").normalizedName("inter").country("Italy").build());

        HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                .competition(comp)
                .season(season)
                .competitionSeason(cs)
                .homeTeam(home)
                .awayTeam(away)
                .matchDate(LocalDate.of(2023, 11, 26))
                .homeGoals(1)
                .awayGoals(1)
                .fullTimeResult("D")
                .homeYellowCards(0) // recorded ZERO yellow cards
                .awayYellowCards(2)
                .homeCorners(null) // NOT recorded (NULL)
                .awayCorners(null) // NOT recorded (NULL)
                .build());

        HistoricalMatch loaded = historicalMatchRepository.findById(match.getId()).orElseThrow();
        assertThat(loaded.getHomeYellowCards()).isEqualTo(0);
        assertThat(loaded.getHomeCorners()).isNull();
        assertThat(loaded.getAwayCorners()).isNull();
    }

    @Test
    @DisplayName("13. Valid decimal betting odds are stored with exact precision")
    void validDecimalOddsStoredCorrectly() {
        Competition comp = competitionRepository.save(Competition.builder().name("Bundesliga").normalizedName("bundesliga").country("Germany").build());
        Season season = seasonRepository.save(Season.builder().seasonCode("2022/2023").seasonName("2022/2023").startDate(LocalDate.of(2022, 8, 1)).endDate(LocalDate.of(2023, 5, 31)).build());
        CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2022, 8, 5)).endDate(LocalDate.of(2023, 5, 27)).build());
        Team home = teamRepository.save(Team.builder().name("Bayern Munich").normalizedName("bayern munich").country("Germany").build());
        Team away = teamRepository.save(Team.builder().name("Dortmund").normalizedName("dortmund").country("Germany").build());

        HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                .competition(comp)
                .season(season)
                .competitionSeason(cs)
                .homeTeam(home)
                .awayTeam(away)
                .matchDate(LocalDate.of(2023, 4, 1))
                .homeWinOdds(new BigDecimal("1.5250"))
                .drawOdds(new BigDecimal("4.8500"))
                .awayWinOdds(new BigDecimal("5.7500"))
                .over25Odds(new BigDecimal("1.4000"))
                .under25Odds(new BigDecimal("2.9500"))
                .bttsYesOdds(new BigDecimal("1.5000"))
                .bttsNoOdds(new BigDecimal("2.5000"))
                .build());

        HistoricalMatch loaded = historicalMatchRepository.findById(match.getId()).orElseThrow();
        assertThat(loaded.getHomeWinOdds()).isEqualByComparingTo(new BigDecimal("1.5250"));
        assertThat(loaded.getUnder25Odds()).isEqualByComparingTo(new BigDecimal("2.9500"));
    }

    @Test
    @DisplayName("15, 16 & 17. Match provenance lineage: match has multiple source records linked to import batch")
    void matchProvenanceLineageIntegrity() {
        Competition comp = competitionRepository.save(Competition.builder().name("Ligue 1").normalizedName("ligue 1").country("France").build());
        Season season = seasonRepository.save(Season.builder().seasonCode("2021/2022").seasonName("2021/2022").startDate(LocalDate.of(2021, 8, 1)).endDate(LocalDate.of(2022, 5, 31)).build());
        CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2021, 8, 6)).endDate(LocalDate.of(2022, 5, 21)).build());
        Team home = teamRepository.save(Team.builder().name("PSG").normalizedName("psg").country("France").build());
        Team away = teamRepository.save(Team.builder().name("Marseille").normalizedName("marseille").country("France").build());

        HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                .competition(comp)
                .season(season)
                .competitionSeason(cs)
                .homeTeam(home)
                .awayTeam(away)
                .matchDate(LocalDate.of(2022, 4, 17))
                .homeGoals(2)
                .awayGoals(1)
                .fullTimeResult("H")
                .build());

        ImportBatch batch1 = importBatchRepository.save(ImportBatch.builder()
                .sourceName("football-data")
                .sourceFileName("F1_2021_2022.csv")
                .sourceFileChecksum("checksum_hash_11111")
                .totalRows(380)
                .insertedRows(380)
                .status("COMPLETED")
                .build());

        ImportBatch batch2 = importBatchRepository.save(ImportBatch.builder()
                .sourceName("alternative-source")
                .sourceFileName("ligue1_archive_2022.csv")
                .sourceFileChecksum("checksum_hash_22222")
                .totalRows(380)
                .updatedRows(380)
                .status("COMPLETED")
                .build());

        MatchSourceRecord src1 = matchSourceRecordRepository.save(MatchSourceRecord.builder()
                .historicalMatch(match)
                .importBatch(batch1)
                .sourceName("football-data")
                .sourceFileName("F1_2021_2022.csv")
                .sourceRowNumber(154)
                .sourceRecordHash("hash_record_row_154")
                .build());

        MatchSourceRecord src2 = matchSourceRecordRepository.save(MatchSourceRecord.builder()
                .historicalMatch(match)
                .importBatch(batch2)
                .sourceName("alternative-source")
                .sourceFileName("ligue1_archive_2022.csv")
                .sourceRowNumber(154)
                .sourceRecordHash("hash_record_row_alt_154")
                .build());

        List<MatchSourceRecord> lineage = matchSourceRecordRepository.findByHistoricalMatchId(match.getId());
        assertThat(lineage).hasSize(2);
        assertThat(lineage).extracting(MatchSourceRecord::getSourceName)
                .containsExactlyInAnyOrder("football-data", "alternative-source");
    }

    @Test
    @DisplayName("18. Quarantine records preserve raw source row information and failure reasons")
    void quarantineRecordsPreserveRowInformation() {
        ImportBatch batch = importBatchRepository.save(ImportBatch.builder()
                .sourceName("football-data")
                .sourceFileName("E0_2024_2025.csv")
                .sourceFileChecksum("chk_123456")
                .status("PARTIAL_SUCCESS")
                .quarantinedRows(1)
                .build());

        ImportQuarantineRecord q = importQuarantineRecordRepository.save(ImportQuarantineRecord.builder()
                .importBatch(batch)
                .sourceFileName("E0_2024_2025.csv")
                .sourceRowNumber(87)
                .rawRowData("E0,2024-11-01,Unknown Team A,Unknown Team B,1,0,H")
                .failureCategory("UNRESOLVED_TEAM_ALIAS")
                .failureReason("Could not resolve team aliases 'Unknown Team A' or 'Unknown Team B'")
                .resolutionStatus("UNRESOLVED")
                .build());

        assertThat(q.getId()).isNotNull();
        assertThat(q.getSourceRowNumber()).isEqualTo(87);
        assertThat(q.getFailureCategory()).isEqualTo("UNRESOLVED_TEAM_ALIAS");
        assertThat(q.getRawRowData()).contains("Unknown Team A");
        assertThat(q.getImportBatch().getId()).isEqualTo(batch.getId());
    }
}
