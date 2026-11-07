package com.VumaPhaseTwo.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionAliasDto {
    private Long id;
    private Long competitionId;
    private String competitionName;
    private String source;
    private String sourceCode;
    private String rawAlias;
    private String normalizedAlias;
    private String countryContext;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
