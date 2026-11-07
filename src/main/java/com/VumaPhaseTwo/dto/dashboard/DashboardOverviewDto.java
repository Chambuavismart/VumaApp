package com.VumaPhaseTwo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewDto {
    private long totalHistoricalMatches;
    private long totalCanonicalTeams;
    private long totalCompetitions;
    private long totalSeasons;
    private long totalCompetitionSeasons;
    private long totalImportBatches;
    private long totalQuarantinedRecords;

    @Builder.Default
    private List<HistoricalDataCoverageDto> dataCoverage = new ArrayList<>();

    @Builder.Default
    private List<RecentImportActivityDto> recentImports = new ArrayList<>();
}
