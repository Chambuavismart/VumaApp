package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonDetailDto {
    private Long id;
    private String seasonCode;
    private String seasonName;
    private LocalDate defaultStartDate;
    private LocalDate defaultEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CompetitionSeasonDto> competitionSeasons;
}
