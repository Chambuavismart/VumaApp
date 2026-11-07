package com.VumaPhaseTwo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentImportActivityDto {
    private Long batchId;
    private Long importGroupId;
    private String sourceName;
    private String sourceFileName;
    private String competitionName;
    private String seasonCode;
    private String seasonName;
    private String status;
    private Integer totalRows;
    private Integer importedRows;
    private Integer quarantinedRows;
    private Integer failedRows;
    private String distributionFormat;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
