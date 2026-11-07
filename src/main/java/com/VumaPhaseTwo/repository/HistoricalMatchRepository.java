package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.HistoricalMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalMatchRepository extends JpaRepository<HistoricalMatch, Long> {
    Optional<HistoricalMatch> findByNaturalMatchKey(String naturalMatchKey);
    List<HistoricalMatch> findByCompetitionIdAndSeasonId(Long competitionId, Long seasonId);
    List<HistoricalMatch> findByMatchDate(LocalDate matchDate);
    List<HistoricalMatch> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);
}
