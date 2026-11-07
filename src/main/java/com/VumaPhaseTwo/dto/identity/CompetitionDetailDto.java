package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDetailDto {
    private Long id;
    private String name;
    private String normalizedName;
    private String country;
    private String competitionType;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CompetitionAliasDto> aliases;
    private List<CompetitionSeasonDto> seasons;
}
