package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonResolutionRequest {
    private String incomingSeasonRepresentation;
    private Long competitionId;
    private String competitionNameOrCode;
    private LocalDate matchDate;
}
