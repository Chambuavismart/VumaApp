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
public class SeasonEligibilitySummary {
    private String suppliedSeasonLabel;
    private Long canonicalSeasonId;
    private String canonicalSeasonCode;
    private String canonicalSeasonName;
    private Long canonicalCompetitionId;
    private String canonicalCompetitionName;
    private Long competitionSeasonId;
    private int detectedRowCount;
    private List<String> availableRequiredFields;
    private List<String> availableOptionalFields;
    private List<String> missingRequiredFields;
    private boolean eligible;
    private String eligibilityReason;
    private List<String> validationErrors;
}
