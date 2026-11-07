package com.VumaPhaseTwo.dto.identity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTeamAliasRequest {
    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Raw alias is required")
    private String rawAlias;

    private String countryContext;
    private String competitionContext;
}
