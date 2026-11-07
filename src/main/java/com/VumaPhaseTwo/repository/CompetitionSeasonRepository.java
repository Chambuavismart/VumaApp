package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.dto.dashboard.HistoricalDataCoverageDto;
import com.VumaPhaseTwo.model.CompetitionSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionSeasonRepository extends JpaRepository<CompetitionSeason, Long> {
    Optional<CompetitionSeason> findByCompetitionIdAndSeasonId(Long competitionId, Long seasonId);
    List<CompetitionSeason> findByCompetitionId(Long competitionId);
    List<CompetitionSeason> findBySeasonId(Long seasonId);

    @Query("SELECT cs FROM CompetitionSeason cs WHERE cs.competition.id = :competitionId AND " +
           ":matchDate >= cs.startDate AND :matchDate <= cs.endDate")
    List<CompetitionSeason> findByCompetitionAndMatchDate(
            @Param("competitionId") Long competitionId,
            @Param("matchDate") LocalDate matchDate
    );

    @Query("SELECT new com.VumaPhaseTwo.dto.dashboard.HistoricalDataCoverageDto(" +
           "cs.competition.id, cs.competition.name, cs.competition.country, " +
           "cs.season.id, cs.season.seasonCode, cs.season.seasonName, " +
           "COUNT(m.id), " +
           "MIN(m.matchDate), MAX(m.matchDate), cs.active) " +
           "FROM CompetitionSeason cs " +
           "LEFT JOIN HistoricalMatch m ON m.competitionSeason.id = cs.id " +
           "GROUP BY cs.id, cs.competition.id, cs.competition.name, cs.competition.country, cs.season.id, cs.season.seasonCode, cs.season.seasonName, cs.active " +
           "ORDER BY cs.competition.name ASC, cs.season.seasonCode DESC")
    List<HistoricalDataCoverageDto> getHistoricalDataCoverage();
}
