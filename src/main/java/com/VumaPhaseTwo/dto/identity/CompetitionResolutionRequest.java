package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionResolutionRequest {
    private String incomingNameOrCode;
    private String provider;
    private String countryContext;
}
