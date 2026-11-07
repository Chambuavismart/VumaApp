package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvValidationReport {
    private String fileName;
    private String fileChecksum;
    private long fileSize;
    private String distributionFormat; // SEASON_SPECIFIC, STACKED_MULTI_SEASON, UNKNOWN_FORMAT
    private String classificationReason;
    private boolean valid;
    private List<String> detectedHeaders;
    private int totalRows;
    private boolean hasSeasonColumn;
    private List<SeasonEligibilitySummary> seasonSummaries;
    private List<String> errors;
    private List<String> warnings;
}
