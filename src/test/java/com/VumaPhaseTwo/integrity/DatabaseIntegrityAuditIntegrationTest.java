package com.VumaPhaseTwo.integrity;

import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DatabaseIntegrityAuditIntegrationTest {

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
    private MatchSourceRecordRepository matchSourceRecordRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("Task 2: Competition and Season Consistency")
    class CompetitionSeasonConsistencyTests {

        @Test
        @Transactional
        @DisplayName("Valid consistent triplet is accepted in historical_matches")
        void validConsistentTripletAccepted() {
            Competition comp = competitionRepository.save(Competition.builder()
                    .name("English Premier League")
                    .normalizedName("english premier league")
                    .country("England")
                    .build());

            Season season = seasonRepository.save(Season.builder()
                    .seasonCode("2024/2025-V2A")
                    .seasonName("2024-2025")
                    .startDate(LocalDate.of(2024, 8, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());

            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp)
                    .season(season)
                    .startDate(LocalDate.of(2024, 8, 15))
                    .endDate(LocalDate.of(2025, 5, 25))
                    .build());

            Team home = teamRepository.save(Team.builder().name("Arsenal FC").normalizedName("arsenal fc").country("England").build());
            Team away = teamRepository.save(Team.builder().name("Chelsea FC").normalizedName("chelsea fc").country("England").build());

            HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp)
                    .season(season)
                    .competitionSeason(cs)
                    .homeTeam(home)
                    .awayTeam(away)
                    .matchDate(LocalDate.of(2024, 11, 10))
                    .naturalMatchKey("ENG-PL-20242025-ARS-CHE-20241110")
                    .build());

            assertThat(match.getId()).isNotNull();
        }

        @Test
        @Transactional
        @DisplayName("Database rejects historical_matches with mismatched competition_id and competition_season_id")
        void rejectsMismatchedCompetitionIdInMatch() {
            Competition comp1 = competitionRepository.save(Competition.builder()
                    .name("Comp Alpha")
                    .normalizedName("comp alpha")
                    .country("England")
                    .build());
            Competition comp2 = competitionRepository.save(Competition.builder()
                    .name("Comp Beta")
                    .normalizedName("comp beta")
                    .country("Spain")
                    .build());
            Season season = seasonRepository.save(Season.builder()
                    .seasonCode("2024/2025-V2B")
                    .seasonName("2024/2025")
                    .startDate(LocalDate.of(2024, 8, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp1)
                    .season(season)
                    .startDate(LocalDate.of(2024, 8, 15))
                    .endDate(LocalDate.of(2025, 5, 25))
                    .build());

            Team home = teamRepository.save(Team.builder().name("Team Alpha").normalizedName("team alpha").country("England").build());
            Team away = teamRepository.save(Team.builder().name("Team Beta").normalizedName("team beta").country("England").build());

            // Attempt to insert match with cs (which belongs to comp1), but pass comp2.getId() as competition_id
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO historical_matches 
                    (competition_id, season_id, competition_season_id, home_team_id, away_team_id, match_date, natural_match_key)
                    VALUES (?, ?, ?, ?, ?, '2024-11-10', 'INCONSISTENT-COMP-TEST-1')
                    """, comp2.getId(), season.getId(), cs.getId(), home.getId(), away.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @Transactional
        @DisplayName("Database rejects historical_matches with mismatched season_id and competition_season_id")
        void rejectsMismatchedSeasonIdInMatch() {
            Competition comp = competitionRepository.save(Competition.builder()
                    .name("Comp Gamma")
                    .normalizedName("comp gamma")
                    .country("Germany")
                    .build());
            Season season1 = seasonRepository.save(Season.builder()
                    .seasonCode("2023/2024-V2C1")
                    .seasonName("2023/2024")
                    .startDate(LocalDate.of(2023, 8, 1))
                    .endDate(LocalDate.of(2024, 5, 31))
                    .build());
            Season season2 = seasonRepository.save(Season.builder()
                    .seasonCode("2024/2025-V2C2")
                    .seasonName("2024/2025")
                    .startDate(LocalDate.of(2024, 8, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp)
                    .season(season1)
                    .startDate(LocalDate.of(2023, 8, 15))
                    .endDate(LocalDate.of(2024, 5, 25))
                    .build());

            Team home = teamRepository.save(Team.builder().name("Team Gamma").normalizedName("team gamma").country("Germany").build());
            Team away = teamRepository.save(Team.builder().name("Team Delta").normalizedName("team delta").country("Germany").build());

            // Attempt to insert match with cs (which belongs to season1), but pass season2.getId() as season_id
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO historical_matches 
                    (competition_id, season_id, competition_season_id, home_team_id, away_team_id, match_date, natural_match_key)
                    VALUES (?, ?, ?, ?, ?, '2023-11-10', 'INCONSISTENT-SEASON-TEST-1')
                    """, comp.getId(), season2.getId(), cs.getId(), home.getId(), away.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @Transactional
        @DisplayName("Database rejects import_batches with mismatched competition, season, and competition_season")
        void rejectsMismatchedImportBatchTriplet() {
            Competition comp1 = competitionRepository.save(Competition.builder()
                    .name("Comp Batch A")
                    .normalizedName("comp batch a")
                    .country("France")
                    .build());
            Competition comp2 = competitionRepository.save(Competition.builder()
                    .name("Comp Batch B")
                    .normalizedName("comp batch b")
                    .country("Italy")
                    .build());
            Season season = seasonRepository.save(Season.builder()
                    .seasonCode("2022/2023-V2D")
                    .seasonName("2022/2023")
                    .startDate(LocalDate.of(2022, 8, 1))
                    .endDate(LocalDate.of(2023, 5, 31))
                    .build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp1)
                    .season(season)
                    .startDate(LocalDate.of(2022, 8, 15))
                    .endDate(LocalDate.of(2023, 5, 25))
                    .build());

            // Attempt to insert import batch with cs belonging to comp1, but specifying comp2.getId()
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO import_batches 
                    (source_name, source_file_name, source_file_checksum, competition_id, season_id, competition_season_id)
                    VALUES ('football-data', 'test_f1.csv', 'checksum12345', ?, ?, ?)
                    """, comp2.getId(), season.getId(), cs.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Task 3: Alias Uniqueness for Nullable Contexts")
    class AliasUniquenessTests {

        @Test
        @Transactional
        @DisplayName("1. Duplicate global aliases (NULL context) are rejected by database")
        void duplicateGlobalAliasesRejected() {
            Competition comp1 = competitionRepository.save(Competition.builder()
                    .name("Premier League Global")
                    .normalizedName("premier league global")
                    .country("England")
                    .build());
            Competition comp2 = competitionRepository.save(Competition.builder()
                    .name("Another League Global")
                    .normalizedName("another league global")
                    .country("England")
                    .build());

            // Insert first global alias (country_context = NULL)
            jdbcTemplate.update("""
                INSERT INTO competition_aliases (competition_id, source, raw_alias, normalized_alias, country_context)
                VALUES (?, 'football-data', 'Premier League', 'premier league', NULL)
                """, comp1.getId());

            // Attempt to insert duplicate global alias from same source with NULL country_context
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO competition_aliases (competition_id, source, raw_alias, normalized_alias, country_context)
                    VALUES (?, 'football-data', 'Premier League', 'premier league', NULL)
                    """, comp2.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @Transactional
        @DisplayName("2. Duplicate contextual aliases are rejected by database")
        void duplicateContextualAliasesRejected() {
            Team team1 = teamRepository.save(Team.builder()
                    .name("Context Team 1")
                    .normalizedName("context team 1")
                    .country("England")
                    .build());
            Team team2 = teamRepository.save(Team.builder()
                    .name("Context Team 2")
                    .normalizedName("context team 2")
                    .country("England")
                    .build());

            jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'football-data', 'Man Utd', 'man utd', 'England', 'Premier League')
                """, team1.getId());

            // Attempt duplicate contextual alias
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                    VALUES (?, 'football-data', 'Man Utd', 'man utd', 'England', 'Premier League')
                    """, team2.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @Transactional
        @DisplayName("3. Same alias belonging to different entities where context legitimately distinguishes them is accepted")
        void sameAliasInDifferentContextsAccepted() {
            Team teamUk = teamRepository.save(Team.builder()
                    .name("Arsenal London")
                    .normalizedName("arsenal london")
                    .country("England")
                    .build());
            Team teamArg = teamRepository.save(Team.builder()
                    .name("Arsenal Sarandi")
                    .normalizedName("arsenal sarandi")
                    .country("Argentina")
                    .build());

            // Same alias 'arsenal' in England vs Argentina
            int r1 = jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'football-data', 'Arsenal', 'arsenal', 'England', 'Premier League')
                """, teamUk.getId());

            int r2 = jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'football-data', 'Arsenal', 'arsenal', 'Argentina', 'Liga Profesional')
                """, teamArg.getId());

            assertThat(r1).isEqualTo(1);
            assertThat(r2).isEqualTo(1);
        }

        @Test
        @Transactional
        @DisplayName("4. Same alias from different providers is accepted")
        void sameAliasFromDifferentProvidersAccepted() {
            Team team = teamRepository.save(Team.builder()
                    .name("Everton FC")
                    .normalizedName("everton fc")
                    .country("England")
                    .build());

            int r1 = jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'football-data', 'Everton', 'everton', NULL, NULL)
                """, team.getId());

            int r2 = jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'api-football', 'Everton', 'everton', NULL, NULL)
                """, team.getId());

            int r3 = jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'opta', 'Everton', 'everton', NULL, NULL)
                """, team.getId());

            assertThat(r1).isEqualTo(1);
            assertThat(r2).isEqualTo(1);
            assertThat(r3).isEqualTo(1);
        }

        @Test
        @Transactional
        @DisplayName("5. NULL country context with populated competition context enforces uniqueness")
        void nullCountryContextEnforcesUniqueness() {
            Team team = teamRepository.save(Team.builder()
                    .name("Celtic FC")
                    .normalizedName("celtic fc")
                    .country("Scotland")
                    .build());

            jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'provider-x', 'Celtic', 'celtic', NULL, 'Premiership')
                """, team.getId());

            // Duplicate with NULL country context and same competition context must be rejected
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                    VALUES (?, 'provider-x', 'Celtic', 'celtic', NULL, 'Premiership')
                    """, team.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @Transactional
        @DisplayName("6. NULL competition context with populated country context enforces uniqueness")
        void nullCompetitionContextEnforcesUniqueness() {
            Team team = teamRepository.save(Team.builder()
                    .name("Rangers FC")
                    .normalizedName("rangers fc")
                    .country("Scotland")
                    .build());

            jdbcTemplate.update("""
                INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                VALUES (?, 'provider-y', 'Rangers', 'rangers', 'Scotland', NULL)
                """, team.getId());

            // Duplicate with NULL competition context and same country context must be rejected
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO team_aliases (team_id, source, raw_alias, normalized_alias, country_context, competition_context)
                    VALUES (?, 'provider-y', 'Rangers', 'rangers', 'Scotland', NULL)
                    """, team.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Task 4: Protect Match Provenance")
    class ProtectMatchProvenanceTests {

        @Test
        @Transactional
        @DisplayName("Deleting historical match with linked source records is rejected with RESTRICT")
        void deletingHistoricalMatchWithSourceRecordsRestricted() {
            Competition comp = competitionRepository.save(Competition.builder()
                    .name("Ligue 1 Prov")
                    .normalizedName("ligue 1 prov")
                    .country("France")
                    .build());
            Season season = seasonRepository.save(Season.builder()
                    .seasonCode("2020/2021-V4")
                    .seasonName("2020/2021")
                    .startDate(LocalDate.of(2020, 8, 1))
                    .endDate(LocalDate.of(2021, 5, 31))
                    .build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp)
                    .season(season)
                    .startDate(LocalDate.of(2020, 8, 15))
                    .endDate(LocalDate.of(2021, 5, 25))
                    .build());
            Team home = teamRepository.save(Team.builder()
                    .name("Lyon")
                    .normalizedName("lyon")
                    .country("France")
                    .build());
            Team away = teamRepository.save(Team.builder()
                    .name("Monaco")
                    .normalizedName("monaco")
                    .country("France")
                    .build());

            HistoricalMatch match = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp)
                    .season(season)
                    .competitionSeason(cs)
                    .homeTeam(home)
                    .awayTeam(away)
                    .matchDate(LocalDate.of(2021, 4, 25))
                    .naturalMatchKey("FRA-L1-20202021-LYO-MON-20210425")
                    .build());

            ImportBatch batch = importBatchRepository.save(ImportBatch.builder()
                    .sourceName("football-data")
                    .sourceFileName("F1_2020_2021.csv")
                    .sourceFileChecksum("hash_prov_batch_1")
                    .totalRows(380)
                    .insertedRows(380)
                    .status("COMPLETED")
                    .build());

            MatchSourceRecord sourceRecord = matchSourceRecordRepository.save(MatchSourceRecord.builder()
                    .historicalMatch(match)
                    .importBatch(batch)
                    .sourceName("football-data")
                    .sourceFileName("F1_2020_2021.csv")
                    .sourceRowNumber(101)
                    .sourceRecordHash("hash_prov_row_101")
                    .build());

            // Attempt to delete historical match — must fail due to ON DELETE RESTRICT
            assertThatThrownBy(() -> {
                jdbcTemplate.update("DELETE FROM historical_matches WHERE id = ?", match.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);

            // Verify match and source record still exist
            Integer matchCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM historical_matches WHERE id = ?", Integer.class, match.getId());
            Integer sourceCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM match_source_records WHERE historical_match_id = ?", Integer.class, match.getId());
            assertThat(matchCount).isEqualTo(1);
            assertThat(sourceCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Task 5: Natural Match Key Identity & Uniqueness Rules")
    class NaturalMatchKeyIdentityTests {

        @Test
        @Transactional
        @DisplayName("1. Same date, different competitions: both fixtures accepted")
        void sameDateDifferentCompetitions() {
            Competition pl = competitionRepository.save(Competition.builder().name("Premier League Key").normalizedName("premier league key").country("England").build());
            Competition ll = competitionRepository.save(Competition.builder().name("La Liga Key").normalizedName("la liga key").country("Spain").build());
            Season season = seasonRepository.save(Season.builder().seasonCode("2024/2025-K1").seasonName("2024/2025").startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());

            CompetitionSeason csPl = competitionSeasonRepository.save(CompetitionSeason.builder().competition(pl).season(season).startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());
            CompetitionSeason csLl = competitionSeasonRepository.save(CompetitionSeason.builder().competition(ll).season(season).startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());

            Team t1 = teamRepository.save(Team.builder().name("Arsenal K1").normalizedName("arsenal k1").country("England").build());
            Team t2 = teamRepository.save(Team.builder().name("Chelsea K1").normalizedName("chelsea k1").country("England").build());
            Team t3 = teamRepository.save(Team.builder().name("Real Madrid K1").normalizedName("real madrid k1").country("Spain").build());
            Team t4 = teamRepository.save(Team.builder().name("Barcelona K1").normalizedName("barcelona k1").country("Spain").build());

            LocalDate matchDate = LocalDate.of(2024, 10, 27);

            HistoricalMatch m1 = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(pl).season(season).competitionSeason(csPl)
                    .homeTeam(t1).awayTeam(t2).matchDate(matchDate)
                    .naturalMatchKey("ENG-PL-20242025-ARS-CHE-20241027")
                    .build());

            HistoricalMatch m2 = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(ll).season(season).competitionSeason(csLl)
                    .homeTeam(t3).awayTeam(t4).matchDate(matchDate)
                    .naturalMatchKey("ESP-LL-20242025-RMA-BAR-20241027")
                    .build());

            assertThat(m1.getId()).isNotNull();
            assertThat(m2.getId()).isNotNull();
            assertThat(m1.getId()).isNotEqualTo(m2.getId());
        }

        @Test
        @Transactional
        @DisplayName("2. Same competition and date, different home/away teams: both fixtures accepted")
        void sameCompetitionAndDateDifferentTeams() {
            Competition comp = competitionRepository.save(Competition.builder().name("Serie A Key").normalizedName("serie a key").country("Italy").build());
            Season season = seasonRepository.save(Season.builder().seasonCode("2024/2025-K2").seasonName("2024/2025").startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());

            Team t1 = teamRepository.save(Team.builder().name("Milan K2").normalizedName("milan k2").country("Italy").build());
            Team t2 = teamRepository.save(Team.builder().name("Inter K2").normalizedName("inter k2").country("Italy").build());
            Team t3 = teamRepository.save(Team.builder().name("Juventus K2").normalizedName("juventus k2").country("Italy").build());
            Team t4 = teamRepository.save(Team.builder().name("Roma K2").normalizedName("roma k2").country("Italy").build());

            LocalDate matchDate = LocalDate.of(2024, 11, 3);

            HistoricalMatch m1 = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp).season(season).competitionSeason(cs)
                    .homeTeam(t1).awayTeam(t2).matchDate(matchDate)
                    .naturalMatchKey("ITA-SA-20242025-MIL-INT-20241103")
                    .build());

            HistoricalMatch m2 = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp).season(season).competitionSeason(cs)
                    .homeTeam(t3).awayTeam(t4).matchDate(matchDate)
                    .naturalMatchKey("ITA-SA-20242025-JUV-ROM-20241103")
                    .build());

            assertThat(m1.getId()).isNotNull();
            assertThat(m2.getId()).isNotNull();
        }

        @Test
        @Transactional
        @DisplayName("3. Same teams playing on different dates (e.g. reverse fixtures): both accepted")
        void sameTeamsPlayingOnDifferentDates() {
            Competition comp = competitionRepository.save(Competition.builder().name("Bundesliga Key").normalizedName("bundesliga key").country("Germany").build());
            Season season = seasonRepository.save(Season.builder().seasonCode("2024/2025-K3").seasonName("2024/2025").startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());

            Team bayern = teamRepository.save(Team.builder().name("Bayern K3").normalizedName("bayern k3").country("Germany").build());
            Team bvb = teamRepository.save(Team.builder().name("Dortmund K3").normalizedName("dortmund k3").country("Germany").build());

            HistoricalMatch firstLeg = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp).season(season).competitionSeason(cs)
                    .homeTeam(bayern).awayTeam(bvb).matchDate(LocalDate.of(2024, 11, 30))
                    .naturalMatchKey("GER-BL-20242025-BAY-BVB-20241130")
                    .build());

            HistoricalMatch secondLeg = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp).season(season).competitionSeason(cs)
                    .homeTeam(bvb).awayTeam(bayern).matchDate(LocalDate.of(2025, 4, 12))
                    .naturalMatchKey("GER-BL-20242025-BVB-BAY-20250412")
                    .build());

            assertThat(firstLeg.getId()).isNotNull();
            assertThat(secondLeg.getId()).isNotNull();
        }

        @Test
        @Transactional
        @DisplayName("4. Same fixture arriving from multiple providers: links to single historical match via source records")
        void sameFixtureFromMultipleProvidersPreservesCanonicalMatch() {
            Competition comp = competitionRepository.save(Competition.builder().name("Eredivisie Key").normalizedName("eredivisie key").country("Netherlands").build());
            Season season = seasonRepository.save(Season.builder().seasonCode("2024/2025-K4").seasonName("2024/2025").startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder().competition(comp).season(season).startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2025, 5, 31)).build());

            Team ajax = teamRepository.save(Team.builder().name("Ajax K4").normalizedName("ajax k4").country("Netherlands").build());
            Team psv = teamRepository.save(Team.builder().name("PSV K4").normalizedName("psv k4").country("Netherlands").build());

            String naturalKey = "NLD-ED-20242025-AJA-PSV-20241102";

            HistoricalMatch canonicalMatch = historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp).season(season).competitionSeason(cs)
                    .homeTeam(ajax).awayTeam(psv).matchDate(LocalDate.of(2024, 11, 2))
                    .naturalMatchKey(naturalKey)
                    .homeGoals(3).awayGoals(2).fullTimeResult("H")
                    .build());

            ImportBatch batch1 = importBatchRepository.save(ImportBatch.builder()
                    .sourceName("football-data")
                    .sourceFileName("N1_2024.csv")
                    .sourceFileChecksum("hash_batch_n1")
                    .status("COMPLETED")
                    .build());

            ImportBatch batch2 = importBatchRepository.save(ImportBatch.builder()
                    .sourceName("api-football")
                    .sourceFileName("eredivisie_2024.json")
                    .sourceFileChecksum("hash_batch_n2")
                    .status("COMPLETED")
                    .build());

            MatchSourceRecord rec1 = matchSourceRecordRepository.save(MatchSourceRecord.builder()
                    .historicalMatch(canonicalMatch)
                    .importBatch(batch1)
                    .sourceName("football-data")
                    .sourceFileName("N1_2024.csv")
                    .sourceRowNumber(75)
                    .sourceRecordHash("rec_hash_1")
                    .build());

            MatchSourceRecord rec2 = matchSourceRecordRepository.save(MatchSourceRecord.builder()
                    .historicalMatch(canonicalMatch)
                    .importBatch(batch2)
                    .sourceName("api-football")
                    .sourceFileName("eredivisie_2024.json")
                    .sourceRowNumber(75)
                    .sourceRecordHash("rec_hash_2")
                    .build());

            assertThat(rec1.getId()).isNotNull();
            assertThat(rec2.getId()).isNotNull();
            assertThat(matchSourceRecordRepository.findByHistoricalMatchId(canonicalMatch.getId())).hasSize(2);
        }

        @Test
        @Transactional
        @DisplayName("5. Duplicate natural match key is rejected by unique constraint")
        void duplicateNaturalMatchKeyRejected() {
            Competition comp = competitionRepository.save(Competition.builder()
                    .name("Primeira Liga Key")
                    .normalizedName("primeira liga key")
                    .country("Portugal")
                    .build());
            Season season = seasonRepository.save(Season.builder()
                    .seasonCode("2024/2025-K5")
                    .seasonName("2024/2025")
                    .startDate(LocalDate.of(2024, 8, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            CompetitionSeason cs = competitionSeasonRepository.save(CompetitionSeason.builder()
                    .competition(comp)
                    .season(season)
                    .startDate(LocalDate.of(2024, 8, 15))
                    .endDate(LocalDate.of(2025, 5, 25))
                    .build());
            Team home = teamRepository.save(Team.builder()
                    .name("Benfica K5")
                    .normalizedName("benfica k5")
                    .country("Portugal")
                    .build());
            Team away = teamRepository.save(Team.builder()
                    .name("Porto K5")
                    .normalizedName("porto k5")
                    .country("Portugal")
                    .build());

            historicalMatchRepository.save(HistoricalMatch.builder()
                    .competition(comp)
                    .season(season)
                    .competitionSeason(cs)
                    .homeTeam(home)
                    .awayTeam(away)
                    .matchDate(LocalDate.of(2024, 11, 10))
                    .naturalMatchKey("POR-PL-20242025-BEN-POR-20241110")
                    .build());

            // Attempt duplicate natural key
            assertThatThrownBy(() -> {
                jdbcTemplate.update("""
                    INSERT INTO historical_matches 
                    (competition_id, season_id, competition_season_id, home_team_id, away_team_id, match_date, natural_match_key)
                    VALUES (?, ?, ?, ?, ?, '2024-11-10', 'POR-PL-20242025-BEN-POR-20241110')
                    """, comp.getId(), season.getId(), cs.getId(), home.getId(), away.getId());
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
