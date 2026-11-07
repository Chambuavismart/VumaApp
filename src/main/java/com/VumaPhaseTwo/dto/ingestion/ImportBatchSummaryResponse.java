package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportBatchSummaryResponse {
    private Long id;
    private Long importGroupId;
    private String sourceName;
    private String sourceFileName;
    private String sourceFileChecksum;
    private String distributionFormat;
    private Long competitionId;
    private String competitionName;
    private Long seasonId;
    private String seasonName;
    private String seasonCode;
    private Long competitionSeasonId;
    private String status;
    private Integer totalRows;
    private Integer successfulRows;
    private Integer insertedRows;
    private Integer updatedRows;
    private Integer skippedRows;
    private Integer quarantinedRows;
    private Integer failedRows;
    private String startedAt;
    private String completedAt;
    private String errorSummary;
}
