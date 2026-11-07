package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDashboardStats {
    private long totalPhysicalFilesReceived;
    private long seasonSpecificFilesCount;
    private long stackedFilesCount;
    private long totalImportGroups;
    private long totalSeasonBatches;
    private long completedBatches;
    private long failedBatches;
    private long partialSuccessBatches;
    private long pendingOrProcessingBatches;
    private long totalImportedMatches;
    private long totalDuplicateMatches;
    private long totalQuarantinedRecords;
    private long totalFailedRecords;
}
