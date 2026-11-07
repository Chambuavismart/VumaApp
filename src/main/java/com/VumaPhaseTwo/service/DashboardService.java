package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.dashboard.DashboardOverviewDto;
import com.VumaPhaseTwo.dto.dashboard.HistoricalDataCoverageDto;
import com.VumaPhaseTwo.dto.dashboard.RecentImportActivityDto;
import com.VumaPhaseTwo.model.ImportBatch;
import com.VumaPhaseTwo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final HistoricalMatchRepository historicalMatchRepository;
    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ImportQuarantineRecordRepository quarantineRecordRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewDto getDashboardOverview() {
        long matchCount = historicalMatchRepository.count();
        long teamCount = teamRepository.count();
        long competitionCount = competitionRepository.count();
        long seasonCount = seasonRepository.count();
        long compSeasonCount = competitionSeasonRepository.count();
        long batchCount = importBatchRepository.count();
        long quarantineCount = quarantineRecordRepository.count();

        List<HistoricalDataCoverageDto> coverage = competitionSeasonRepository.getHistoricalDataCoverage();

        List<ImportBatch> recentBatches = importBatchRepository.findTop15ByOrderByStartedAtDesc();
        List<RecentImportActivityDto> recentImports = recentBatches.stream()
                .map(this::mapToRecentImportActivity)
                .collect(Collectors.toList());

        return DashboardOverviewDto.builder()
                .totalHistoricalMatches(matchCount)
                .totalCanonicalTeams(teamCount)
                .totalCompetitions(competitionCount)
                .totalSeasons(seasonCount)
                .totalCompetitionSeasons(compSeasonCount)
                .totalImportBatches(batchCount)
                .totalQuarantinedRecords(quarantineCount)
                .dataCoverage(coverage)
                .recentImports(recentImports)
                .build();
    }

    private RecentImportActivityDto mapToRecentImportActivity(ImportBatch batch) {
        String compName = null;
        if (batch.getCompetition() != null) {
            compName = batch.getCompetition().getName();
        }

        String seasonCode = null;
        String seasonName = null;
        if (batch.getSeason() != null) {
            seasonCode = batch.getSeason().getSeasonCode();
            seasonName = batch.getSeason().getSeasonName();
        }

        Long groupId = null;
        if (batch.getImportGroup() != null) {
            groupId = batch.getImportGroup().getId();
        }

        int imported = (batch.getInsertedRows() != null ? batch.getInsertedRows() : 0) +
                       (batch.getUpdatedRows() != null ? batch.getUpdatedRows() : 0);

        return RecentImportActivityDto.builder()
                .batchId(batch.getId())
                .importGroupId(groupId)
                .sourceName(batch.getSourceName())
                .sourceFileName(batch.getSourceFileName())
                .competitionName(compName)
                .seasonCode(seasonCode)
                .seasonName(seasonName)
                .status(batch.getStatus())
                .totalRows(batch.getTotalRows())
                .importedRows(imported)
                .quarantinedRows(batch.getQuarantinedRows())
                .failedRows(batch.getFailedRows())
                .distributionFormat(batch.getDistributionFormat())
                .startedAt(batch.getStartedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
