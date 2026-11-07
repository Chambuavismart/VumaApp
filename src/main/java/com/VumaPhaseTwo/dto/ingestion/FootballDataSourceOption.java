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
public class FootballDataSourceOption {
    private String competitionCode;
    private String competitionName;
    private String country;
    private String distributionFormat; // SEASON_SPECIFIC or STACKED_MULTI_SEASON
    private String seasonLabel; // e.g. "2024/2025" or "Multi-Season (2016-2025)"
    private String seasonCode; // e.g. "2425" or "stacked"
    private String downloadUrl;
    private boolean verified;
    private String notes;
}
