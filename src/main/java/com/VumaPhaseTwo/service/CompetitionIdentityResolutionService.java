package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.Competition;
import com.VumaPhaseTwo.model.CompetitionAlias;
import com.VumaPhaseTwo.repository.CompetitionAliasRepository;
import com.VumaPhaseTwo.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionIdentityResolutionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionAliasRepository competitionAliasRepository;
    private final IdentityNormalizationService normalizationService;

    /**
     * Resolves incoming competition names or provider codes (e.g. E0, E1, Premier League)
     * to canonical competitions and registered competition aliases.
     */
    @Transactional(readOnly = true)
    public CompetitionResolutionResult resolve(CompetitionResolutionRequest request) {
        if (request == null || request.getIncomingNameOrCode() == null || request.getIncomingNameOrCode().trim().isEmpty()) {
            return CompetitionResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .originalInput(request != null ? request.getIncomingNameOrCode() : null)
                    .provider(request != null ? request.getProvider() : null)
                    .explanation("Incoming competition name or code is null, empty or whitespace")
                    .build();
        }

        String rawInput = request.getIncomingNameOrCode().trim();
        String normalizedInput = normalizationService.normalizeCompetitionName(rawInput);
        String provider = request.getProvider() != null && !request.getProvider().trim().isEmpty()
                ? normalizationService.normalize(request.getProvider())
                : null;
        String countryContext = request.getCountryContext() != null && !request.getCountryContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCountryContext())
                : null;

        if (normalizedInput.isEmpty()) {
            return CompetitionResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .originalInput(rawInput)
                    .normalizedInput(normalizedInput)
                    .provider(provider)
                    .explanation("Competition input became empty after normalization")
                    .build();
        }

        // STEP 1: Provider-specific exact alias / source_code lookup
        if (provider != null) {
            // Check by source and source_code (e.g. provider='football-data', source_code='E0' or rawInput)
            List<CompetitionAlias> codeAliases = competitionAliasRepository.findBySourceAndSourceCode(provider, rawInput);
            if (codeAliases.isEmpty()) {
                codeAliases = competitionAliasRepository.findBySourceAndSourceCode(provider, rawInput.toUpperCase(Locale.ROOT));
            }

            if (!codeAliases.isEmpty()) {
                CompetitionResolutionResult result = evaluateAliasCandidates(
                        codeAliases, rawInput, normalizedInput, provider, countryContext, "provider source code (" + rawInput + ")");
                if (result != null) {
                    return result;
                }
            }

            // Check by exact scoped alias (source, normalized_alias, country_context)
            Optional<CompetitionAlias> exactScoped = competitionAliasRepository.findScopedAlias(
                    provider, normalizedInput, countryContext);
            if (exactScoped.isPresent() && Boolean.TRUE.equals(exactScoped.get().getActive())) {
                Competition comp = exactScoped.get().getCompetition();
                return buildResolved(rawInput, normalizedInput, comp, exactScoped.get().getId(), provider,
                        countryContext, "Resolved via exact provider competition alias mapping");
            }

            // Check broad provider aliases for this normalized alias
            List<CompetitionAlias> providerAliases = competitionAliasRepository.findBySourceAndNormalizedAlias(provider, normalizedInput);
            if (!providerAliases.isEmpty()) {
                CompetitionResolutionResult result = evaluateAliasCandidates(
                        providerAliases, rawInput, normalizedInput, provider, countryContext, "provider alias");
                if (result != null) {
                    return result;
                }
            }
        }

        // STEP 2: Canonical competition direct match
        List<Competition> canonicalMatches = competitionRepository.findByNormalizedName(normalizedInput);
        if (!canonicalMatches.isEmpty()) {
            if (countryContext != null) {
                List<Competition> countryFiltered = canonicalMatches.stream()
                        .filter(c -> normalizationService.normalize(c.getCountry()).equalsIgnoreCase(countryContext))
                        .toList();

                if (countryFiltered.size() == 1) {
                    return buildResolved(rawInput, normalizedInput, countryFiltered.get(0), null, provider,
                            countryContext, "Resolved via exact canonical competition name and country match");
                } else if (countryFiltered.size() > 1) {
                    return buildAmbiguous(rawInput, normalizedInput, provider, countryContext,
                            "Multiple canonical competitions with name '" + normalizedInput + "' in country: " + countryContext,
                            toCandidates(countryFiltered, "Canonical competition in country"));
                }
            } else {
                if (canonicalMatches.size() == 1) {
                    return buildResolved(rawInput, normalizedInput, canonicalMatches.get(0), null, provider,
                            countryContext, "Resolved via unique canonical competition name");
                } else {
                    return buildAmbiguous(rawInput, normalizedInput, provider, countryContext,
                            "Multiple canonical competitions exist with name '" + normalizedInput + "'. Provide country context.",
                            toCandidates(canonicalMatches, "Canonical competition in "));
                }
            }
        }

        // STEP 3: Cross-provider global competition alias lookup
        List<CompetitionAlias> globalAliases = competitionAliasRepository.findByNormalizedAlias(normalizedInput);
        if (!globalAliases.isEmpty()) {
            CompetitionResolutionResult globalResult = evaluateAliasCandidates(
                    globalAliases, rawInput, normalizedInput, provider, countryContext, "cross-provider competition alias");
            if (globalResult != null) {
                return globalResult;
            }
        }

        // Unresolved
        return CompetitionResolutionResult.builder()
                .status(ResolutionStatus.UNRESOLVED)
                .originalInput(rawInput)
                .normalizedInput(normalizedInput)
                .provider(provider)
                .countryContext(countryContext)
                .explanation("No canonical competition or registered alias matched '" + rawInput + "'")
                .candidates(Collections.emptyList())
                .build();
    }

    private CompetitionResolutionResult evaluateAliasCandidates(
            List<CompetitionAlias> aliases,
            String rawInput,
            String normalizedInput,
            String provider,
            String countryContext,
            String sourceDescription
    ) {
        List<CompetitionAlias> active = aliases.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .toList();

        if (active.isEmpty()) {
            return null;
        }

        List<CompetitionAlias> filtered = active;
        if (countryContext != null) {
            filtered = active.stream()
                    .filter(a -> a.getCountryContext() == null ||
                                 normalizationService.normalize(a.getCountryContext()).equalsIgnoreCase(countryContext) ||
                                 normalizationService.normalize(a.getCompetition().getCountry()).equalsIgnoreCase(countryContext))
                    .toList();
        }

        Map<Long, List<CompetitionAlias>> byCompetition = filtered.stream()
                .collect(Collectors.groupingBy(a -> a.getCompetition().getId()));

        if (byCompetition.size() == 1) {
            CompetitionAlias matched = filtered.get(0);
            return buildResolved(rawInput, normalizedInput, matched.getCompetition(), matched.getId(), provider,
                    countryContext, "Resolved via " + sourceDescription + " mapping");
        } else if (byCompetition.size() > 1) {
            List<CanonicalCandidateDto> candidates = byCompetition.values().stream()
                    .map(list -> {
                        Competition c = list.get(0).getCompetition();
                        return CanonicalCandidateDto.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .country(c.getCountry())
                                .reason("Mapped via competition alias ID " + list.get(0).getId() + " (" + list.get(0).getSource() + ")")
                                .build();
                    })
                    .toList();

            return buildAmbiguous(rawInput, normalizedInput, provider, countryContext,
                    "Ambiguous: " + byCompetition.size() + " different canonical competitions match via " + sourceDescription,
                    candidates);
        }

        return null;
    }

    private CompetitionResolutionResult buildResolved(
            String rawInput,
            String normalizedInput,
            Competition competition,
            Long aliasId,
            String provider,
            String countryContext,
            String explanation
    ) {
        return CompetitionResolutionResult.builder()
                .status(ResolutionStatus.RESOLVED)
                .originalInput(rawInput)
                .normalizedInput(normalizedInput)
                .resolvedCompetitionId(competition.getId())
                .resolvedCompetitionName(competition.getName())
                .resolvedCompetitionCountry(competition.getCountry())
                .matchedAliasId(aliasId)
                .provider(provider)
                .countryContext(countryContext)
                .explanation(explanation)
                .candidates(Collections.singletonList(CanonicalCandidateDto.builder()
                        .id(competition.getId())
                        .name(competition.getName())
                        .country(competition.getCountry())
                        .reason(explanation)
                        .build()))
                .build();
    }

    private CompetitionResolutionResult buildAmbiguous(
            String rawInput,
            String normalizedInput,
            String provider,
            String countryContext,
            String explanation,
            List<CanonicalCandidateDto> candidates
    ) {
        return CompetitionResolutionResult.builder()
                .status(ResolutionStatus.AMBIGUOUS)
                .originalInput(rawInput)
                .normalizedInput(normalizedInput)
                .provider(provider)
                .countryContext(countryContext)
                .explanation(explanation)
                .candidates(candidates)
                .build();
    }

    private List<CanonicalCandidateDto> toCandidates(List<Competition> competitions, String prefix) {
        return competitions.stream()
                .map(c -> CanonicalCandidateDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .country(c.getCountry())
                        .reason(prefix + ": " + c.getCountry())
                        .build())
                .toList();
    }
}
