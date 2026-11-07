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
public class TeamResolutionResult {
    private ResolutionStatus status;
    private String originalName;
    private String normalizedName;
    private Long resolvedTeamId;
    private String resolvedTeamName;
    private Long matchedAliasId;
    private String provider;
    private String countryContext;
    private String competitionContext;
    private String explanation;
    private List<CanonicalCandidateDto> candidates;
}
