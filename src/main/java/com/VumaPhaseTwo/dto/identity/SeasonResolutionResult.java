package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResolutionResult {
    private ResolutionStatus status;
    private String originalInput;
    private String normalizedSeasonCode;
    private Long resolvedSeasonId;
    private String resolvedSeasonCode;
    private String resolvedSeasonName;
    private Long resolvedCompetitionSeasonId;
    private Long competitionId;
    private String competitionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String explanation;
    private List<CanonicalCandidateDto> candidates;
}
