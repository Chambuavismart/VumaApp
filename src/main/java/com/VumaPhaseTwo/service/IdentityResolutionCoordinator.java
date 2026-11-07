package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityResolutionCoordinator {

    private final TeamIdentityResolutionService teamResolutionService;
    private final CompetitionIdentityResolutionService competitionResolutionService;
    private final SeasonIdentityResolutionService seasonResolutionService;

    public TeamResolutionResult resolveTeam(String incomingName, String provider, String countryContext, String competitionContext) {
        return teamResolutionService.resolve(TeamResolutionRequest.builder()
                .incomingName(incomingName)
                .provider(provider)
                .countryContext(countryContext)
                .competitionContext(competitionContext)
                .build());
    }

    public CompetitionResolutionResult resolveCompetition(String incomingNameOrCode, String provider, String countryContext) {
        return competitionResolutionService.resolve(CompetitionResolutionRequest.builder()
                .incomingNameOrCode(incomingNameOrCode)
                .provider(provider)
                .countryContext(countryContext)
                .build());
    }

    public SeasonResolutionResult resolveSeason(String incomingSeason, Long competitionId, String competitionNameOrCode, LocalDate matchDate) {
        return seasonResolutionService.resolve(SeasonResolutionRequest.builder()
                .incomingSeasonRepresentation(incomingSeason)
                .competitionId(competitionId)
                .competitionNameOrCode(competitionNameOrCode)
                .matchDate(matchDate)
                .build());
    }
}
