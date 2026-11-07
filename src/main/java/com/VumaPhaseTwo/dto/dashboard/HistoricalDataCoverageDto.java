package com.VumaPhaseTwo.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalDataCoverageDto {
    private Long competitionId;
    private String competitionName;
    private String country;
    private Long seasonId;
    private String seasonCode;
    private String seasonName;
    private long matchCount;
    private LocalDate firstMatchDate;
    private LocalDate lastMatchDate;
    private Boolean active;
}
