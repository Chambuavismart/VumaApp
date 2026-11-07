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
public class RegisterCompetitionAliasRequest {
    @NotBlank(message = "Source is required")
    private String source;

    private String sourceCode;

    @NotBlank(message = "Raw alias is required")
    private String rawAlias;

    private String countryContext;
}
