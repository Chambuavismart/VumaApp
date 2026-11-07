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
public class TeamAliasDto {
    private Long id;
    private Long teamId;
    private String teamName;
    private String source;
    private String rawAlias;
    private String normalizedAlias;
    private String countryContext;
    private String competitionContext;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
