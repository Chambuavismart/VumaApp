package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportGroupResponse {
    private Long id;
    private String sourceName;
    private String sourceFileName;
    private String sourceFileChecksum;
    private String sourceUrl;
    private String distributionFormat;
    private String status;
    private Integer totalPhysicalRows;
    private Integer totalDetectedSeasons;
    private String createdAt;
    private String updatedAt;
    private List<ImportBatchSummaryResponse> batches;
}
