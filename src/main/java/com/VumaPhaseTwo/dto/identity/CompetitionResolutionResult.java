package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionResolutionResult {
    private ResolutionStatus status;
    private String originalInput;
    private String normalizedInput;
    private Long resolvedCompetitionId;
    private String resolvedCompetitionName;
    private String resolvedCompetitionCountry;
    private Long matchedAliasId;
    private String provider;
    private String countryContext;
    private String explanation;
    private List<CanonicalCandidateDto> candidates;
}
