package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.Team;
import com.VumaPhaseTwo.model.TeamAlias;
import com.VumaPhaseTwo.repository.TeamAliasRepository;
import com.VumaPhaseTwo.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamIdentityResolutionService {

    private final TeamRepository teamRepository;
    private final TeamAliasRepository teamAliasRepository;
    private final IdentityNormalizationService normalizationService;

    /**
     * Resolves an incoming team identity against canonical teams and registered team aliases.
     * Follows deterministic waterfall matching:
     * 1. Validate input
     * 2. Normalize input and contexts
     * 3. Exact scoped provider alias lookup (source, normalized_alias, country_context, competition_context)
     * 4. Broad provider alias lookup (source, normalized_alias) with context filtering/ambiguity check
     * 5. Canonical direct match (normalized_name, country)
     * 6. Global cross-provider alias lookup (normalized_alias) with context filtering
     * 7. Base name match (with common suffix stripping)
     * 8. Ambiguity & Unresolved handling
     */
    @Transactional(readOnly = true)
    public TeamResolutionResult resolve(TeamResolutionRequest request) {
        if (request == null || request.getIncomingName() == null || request.getIncomingName().trim().isEmpty()) {
            return TeamResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .originalName(request != null ? request.getIncomingName() : null)
                    .provider(request != null ? request.getProvider() : null)
                    .explanation("Incoming team name is null, empty or contains only whitespace")
                    .build();
        }

        String rawName = request.getIncomingName().trim();
        String normalizedName = normalizationService.normalize(rawName);
        String baseNormalizedName = normalizationService.normalizeTeamName(rawName);

        String provider = request.getProvider() != null && !request.getProvider().trim().isEmpty()
                ? normalizationService.normalize(request.getProvider())
                : null;
        String countryContext = request.getCountryContext() != null && !request.getCountryContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCountryContext())
                : null;
        String competitionContext = request.getCompetitionContext() != null && !request.getCompetitionContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCompetitionContext())
                : null;

        if (normalizedName.isEmpty()) {
            return TeamResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .originalName(rawName)
                    .normalizedName(normalizedName)
                    .provider(provider)
                    .explanation("Team name became empty after normalization")
                    .build();
        }

        // STEP 1: Provider-specific exact alias lookup with full context
        if (provider != null) {
            Optional<TeamAlias> exactAlias = teamAliasRepository.findScopedAlias(
                    provider, normalizedName, countryContext, competitionContext);

            if (exactAlias.isPresent() && Boolean.TRUE.equals(exactAlias.get().getActive())) {
                Team team = exactAlias.get().getTeam();
                return buildResolved(rawName, normalizedName, team, exactAlias.get().getId(), provider,
                        countryContext, competitionContext, "Resolved via exact provider-scoped alias mapping");
            }

            // Also check stripped base name alias
            if (!baseNormalizedName.equals(normalizedName)) {
                Optional<TeamAlias> baseAlias = teamAliasRepository.findScopedAlias(
                        provider, baseNormalizedName, countryContext, competitionContext);
                if (baseAlias.isPresent() && Boolean.TRUE.equals(baseAlias.get().getActive())) {
                    Team team = baseAlias.get().getTeam();
                    return buildResolved(rawName, normalizedName, team, baseAlias.get().getId(), provider,
                            countryContext, competitionContext, "Resolved via provider-scoped base alias mapping");
                }
            }

            // Provider aliases with matching normalized alias (may have broader/different contexts)
            List<TeamAlias> providerAliases = teamAliasRepository.findBySourceAndNormalizedAlias(provider, normalizedName);
            if (providerAliases.isEmpty() && !baseNormalizedName.equals(normalizedName)) {
                providerAliases = teamAliasRepository.findBySourceAndNormalizedAlias(provider, baseNormalizedName);
            }

            if (!providerAliases.isEmpty()) {
                TeamResolutionResult contextResult = evaluateAliasCandidates(
                        providerAliases, rawName, normalizedName, provider, countryContext, competitionContext, "provider alias");
                if (contextResult != null) {
                    return contextResult;
                }
            }
        }

        // STEP 2: Direct match against canonical teams table
        List<Team> canonicalMatches = teamRepository.findByNormalizedName(normalizedName);
        if (canonicalMatches.isEmpty() && !baseNormalizedName.equals(normalizedName)) {
            canonicalMatches = teamRepository.findByNormalizedName(baseNormalizedName);
        }

        if (!canonicalMatches.isEmpty()) {
            // Filter by country if countryContext is provided
            if (countryContext != null) {
                List<Team> countryFiltered = canonicalMatches.stream()
                        .filter(t -> normalizationService.normalize(t.getCountry()).equalsIgnoreCase(countryContext))
                        .toList();

                if (countryFiltered.size() == 1) {
                    return buildResolved(rawName, normalizedName, countryFiltered.get(0), null, provider,
                            countryContext, competitionContext, "Resolved via exact canonical name and country match");
                } else if (countryFiltered.size() > 1) {
                    return buildAmbiguous(rawName, normalizedName, provider, countryContext, competitionContext,
                            "Multiple canonical teams found with matching name in country: " + countryContext,
                            toCandidates(countryFiltered, "Canonical team in country"));
                }
            } else {
                if (canonicalMatches.size() == 1) {
                    return buildResolved(rawName, normalizedName, canonicalMatches.get(0), null, provider,
                            countryContext, competitionContext, "Resolved via unique canonical team normalized name");
                } else {
                    return buildAmbiguous(rawName, normalizedName, provider, countryContext, competitionContext,
                            "Multiple canonical teams exist with name '" + normalizedName + "' across different countries. Provide country context.",
                            toCandidates(canonicalMatches, "Canonical team in " ));
                }
            }
        }

        // STEP 3: Cross-provider global alias matching
        List<TeamAlias> globalAliases = teamAliasRepository.findByNormalizedAlias(normalizedName);
        if (globalAliases.isEmpty() && !baseNormalizedName.equals(normalizedName)) {
            globalAliases = teamAliasRepository.findByNormalizedAlias(baseNormalizedName);
        }

        if (!globalAliases.isEmpty()) {
            TeamResolutionResult globalResult = evaluateAliasCandidates(
                    globalAliases, rawName, normalizedName, provider, countryContext, competitionContext, "cross-provider alias");
            if (globalResult != null) {
                return globalResult;
            }
        }

        // Unresolved
        return TeamResolutionResult.builder()
                .status(ResolutionStatus.UNRESOLVED)
                .originalName(rawName)
                .normalizedName(normalizedName)
                .provider(provider)
                .countryContext(countryContext)
                .competitionContext(competitionContext)
                .explanation("No canonical team or registered alias matched '" + rawName + "'")
                .candidates(Collections.emptyList())
                .build();
    }

    private TeamResolutionResult evaluateAliasCandidates(
            List<TeamAlias> aliases,
            String rawName,
            String normalizedName,
            String provider,
            String countryContext,
            String competitionContext,
            String sourceDescription
    ) {
        List<TeamAlias> activeAliases = aliases.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .toList();

        if (activeAliases.isEmpty()) {
            return null;
        }

        // Filter by country if country context given
        List<TeamAlias> filtered = activeAliases;
        if (countryContext != null) {
            filtered = activeAliases.stream()
                    .filter(a -> a.getCountryContext() == null ||
                                 normalizationService.normalize(a.getCountryContext()).equalsIgnoreCase(countryContext) ||
                                 normalizationService.normalize(a.getTeam().getCountry()).equalsIgnoreCase(countryContext))
                    .toList();
        }

        // Group by canonical team
        Map<Long, List<TeamAlias>> byTeam = filtered.stream()
                .collect(Collectors.groupingBy(a -> a.getTeam().getId()));

        if (byTeam.size() == 1) {
            TeamAlias matched = filtered.get(0);
            return buildResolved(rawName, normalizedName, matched.getTeam(), matched.getId(), provider,
                    countryContext, competitionContext, "Resolved via " + sourceDescription + " mapping");
        } else if (byTeam.size() > 1) {
            List<CanonicalCandidateDto> candidates = byTeam.values().stream()
                    .map(list -> {
                        Team t = list.get(0).getTeam();
                        return CanonicalCandidateDto.builder()
                                .id(t.getId())
                                .name(t.getName())
                                .country(t.getCountry())
                                .reason("Mapped via alias ID " + list.get(0).getId() + " (" + list.get(0).getSource() + ")")
                                .build();
                    })
                    .toList();

            return buildAmbiguous(rawName, normalizedName, provider, countryContext, competitionContext,
                    "Ambiguous: " + byTeam.size() + " different canonical teams match via " + sourceDescription,
                    candidates);
        }

        return null;
    }

    private TeamResolutionResult buildResolved(
            String rawName,
            String normalizedName,
            Team team,
            Long aliasId,
            String provider,
            String countryContext,
            String competitionContext,
            String explanation
    ) {
        return TeamResolutionResult.builder()
                .status(ResolutionStatus.RESOLVED)
                .originalName(rawName)
                .normalizedName(normalizedName)
                .resolvedTeamId(team.getId())
                .resolvedTeamName(team.getName())
                .matchedAliasId(aliasId)
                .provider(provider)
                .countryContext(countryContext)
                .competitionContext(competitionContext)
                .explanation(explanation)
                .candidates(Collections.singletonList(CanonicalCandidateDto.builder()
                        .id(team.getId())
                        .name(team.getName())
                        .country(team.getCountry())
                        .reason(explanation)
                        .build()))
                .build();
    }

    private TeamResolutionResult buildAmbiguous(
            String rawName,
            String normalizedName,
            String provider,
            String countryContext,
            String competitionContext,
            String explanation,
            List<CanonicalCandidateDto> candidates
    ) {
        return TeamResolutionResult.builder()
                .status(ResolutionStatus.AMBIGUOUS)
                .originalName(rawName)
                .normalizedName(normalizedName)
                .provider(provider)
                .countryContext(countryContext)
                .competitionContext(competitionContext)
                .explanation(explanation)
                .candidates(candidates)
                .build();
    }

    private List<CanonicalCandidateDto> toCandidates(List<Team> teams, String prefix) {
        return teams.stream()
                .map(t -> CanonicalCandidateDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .country(t.getCountry())
                        .reason(prefix + ": " + t.getCountry())
                        .build())
                .toList();
    }
}
