package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityOverviewDto {
    private long totalCanonicalTeams;
    private long totalCanonicalCompetitions;
    private long totalSeasons;
    private long totalCompetitionSeasons;
    private long totalTeamAliases;
    private long totalCompetitionAliases;
    private Map<String, Long> teamAliasesBySource;
    private Map<String, Long> competitionAliasesBySource;
}
