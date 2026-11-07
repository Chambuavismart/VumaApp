package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.Competition;
import com.VumaPhaseTwo.model.CompetitionSeason;
import com.VumaPhaseTwo.model.Season;
import com.VumaPhaseTwo.repository.CompetitionRepository;
import com.VumaPhaseTwo.repository.CompetitionSeasonRepository;
import com.VumaPhaseTwo.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonIdentityResolutionService {

    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;
    private final CompetitionRepository competitionRepository;
    private final IdentityNormalizationService normalizationService;

    /**
     * Resolves incoming season representations (e.g. "2024/25", "2024-2025", "2024")
     * or date-based fixtures to canonical seasons and competition_seasons.
     */
    @Transactional(readOnly = true)
    public SeasonResolutionResult resolve(SeasonResolutionRequest request) {
        if (request == null) {
            return SeasonResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .explanation("Season resolution request is null")
                    .build();
        }

        String rawSeason = request.getIncomingSeasonRepresentation();
        Long competitionId = request.getCompetitionId();
        String competitionNameOrCode = request.getCompetitionNameOrCode();
        LocalDate matchDate = request.getMatchDate();

        // Must have at least a season representation or a match date
        if ((rawSeason == null || rawSeason.trim().isEmpty()) && matchDate == null) {
            return SeasonResolutionResult.builder()
                    .status(ResolutionStatus.INVALID_INPUT)
                    .explanation("Either incoming season representation or match date must be provided")
                    .build();
        }

        // Identify competition if provided by name/code
        Competition competition = null;
        if (competitionId != null) {
            competition = competitionRepository.findById(competitionId).orElse(null);
        } else if (competitionNameOrCode != null && !competitionNameOrCode.trim().isEmpty()) {
            String normComp = normalizationService.normalizeCompetitionName(competitionNameOrCode);
            List<Competition> comps = competitionRepository.findByNormalizedName(normComp);
            if (comps.size() == 1) {
                competition = comps.get(0);
                competitionId = competition.getId();
            }
        }

        // SCENARIO 1: Date-based resolution within competition
        if (matchDate != null && competition != null) {
            List<CompetitionSeason> matchingCompSeasons = competitionSeasonRepository.findByCompetitionAndMatchDate(
                    competition.getId(), matchDate);

            if (matchingCompSeasons.size() == 1) {
                CompetitionSeason cs = matchingCompSeasons.get(0);
                Season s = cs.getSeason();
                return buildResolved(
                        rawSeason != null ? rawSeason : matchDate.toString(),
                        s.getSeasonCode(),
                        s,
                        cs,
                        competition,
                        "Resolved via match date (" + matchDate + ") within competition-season boundaries (" + cs.getStartDate() + " to " + cs.getEndDate() + ")"
                );
            } else if (matchingCompSeasons.size() > 1) {
                return buildAmbiguous(
                        rawSeason != null ? rawSeason : matchDate.toString(),
                        null,
                        competition,
                        "Ambiguous: Match date " + matchDate + " falls into multiple configured seasons for competition " + competition.getName(),
                        toCandidates(matchingCompSeasons)
                );
            } else if (rawSeason == null || rawSeason.trim().isEmpty()) {
                return SeasonResolutionResult.builder()
                        .status(ResolutionStatus.UNRESOLVED)
                        .originalInput(matchDate.toString())
                        .competitionId(competition.getId())
                        .competitionName(competition.getName())
                        .explanation("Match date " + matchDate + " does not fall within any configured season boundaries for competition " + competition.getName())
                        .build();
            }
        }

        // SCENARIO 2: String/Code-based season resolution
        if (rawSeason != null && !rawSeason.trim().isEmpty()) {
            String normalizedSeason = normalizationService.normalizeSeasonCode(rawSeason);

            // 1. Look up by exact season code in seasons table
            Optional<Season> exactSeason = seasonRepository.findBySeasonCode(normalizedSeason);
            if (exactSeason.isEmpty() && !normalizedSeason.equals(rawSeason.trim())) {
                exactSeason = seasonRepository.findBySeasonCode(rawSeason.trim());
            }

            if (exactSeason.isPresent()) {
                Season season = exactSeason.get();

                // If competition is present, verify/find competition_season relationship
                if (competition != null) {
                    Optional<CompetitionSeason> compSeason = competitionSeasonRepository.findByCompetitionIdAndSeasonId(
                            competition.getId(), season.getId());

                    if (compSeason.isPresent()) {
                        return buildResolved(rawSeason, normalizedSeason, season, compSeason.get(), competition,
                                "Resolved via exact season code and competition-season relationship");
                    } else {
                        // Competition exists, but this season is not configured for it
                        return SeasonResolutionResult.builder()
                                .status(ResolutionStatus.UNRESOLVED)
                                .originalInput(rawSeason)
                                .normalizedSeasonCode(normalizedSeason)
                                .resolvedSeasonId(season.getId())
                                .resolvedSeasonCode(season.getSeasonCode())
                                .resolvedSeasonName(season.getSeasonName())
                                .competitionId(competition.getId())
                                .competitionName(competition.getName())
                                .explanation("Season '" + season.getSeasonCode() + "' is valid globally, but not configured for competition '" + competition.getName() + "'")
                                .build();
                    }
                } else {
                    // Global season resolved without competition context
                    return SeasonResolutionResult.builder()
                            .status(ResolutionStatus.RESOLVED)
                            .originalInput(rawSeason)
                            .normalizedSeasonCode(normalizedSeason)
                            .resolvedSeasonId(season.getId())
                            .resolvedSeasonCode(season.getSeasonCode())
                            .resolvedSeasonName(season.getSeasonName())
                            .startDate(season.getStartDate())
                            .endDate(season.getEndDate())
                            .explanation("Resolved canonical global season code '" + season.getSeasonCode() + "' (no competition context provided)")
                            .candidates(Collections.singletonList(CanonicalCandidateDto.builder()
                                    .id(season.getId())
                                    .name(season.getSeasonName())
                                    .reason("Canonical season code " + season.getSeasonCode())
                                    .build()))
                            .build();
                }
            }

            // 2. Fuzzy search / partial matches
            List<Season> searchSeasons = seasonRepository.findBySeasonCodeContainingIgnoreCaseOrSeasonNameContainingIgnoreCase(
                    normalizedSeason, normalizedSeason);

            if (searchSeasons.size() == 1) {
                Season season = searchSeasons.get(0);
                if (competition != null) {
                    Optional<CompetitionSeason> compSeason = competitionSeasonRepository.findByCompetitionIdAndSeasonId(
                            competition.getId(), season.getId());
                    if (compSeason.isPresent()) {
                        return buildResolved(rawSeason, normalizedSeason, season, compSeason.get(), competition,
                                "Resolved via season name match and competition-season link");
                    }
                } else {
                    return SeasonResolutionResult.builder()
                            .status(ResolutionStatus.RESOLVED)
                            .originalInput(rawSeason)
                            .normalizedSeasonCode(normalizedSeason)
                            .resolvedSeasonId(season.getId())
                            .resolvedSeasonCode(season.getSeasonCode())
                            .resolvedSeasonName(season.getSeasonName())
                            .startDate(season.getStartDate())
                            .endDate(season.getEndDate())
                            .explanation("Resolved canonical season '" + season.getSeasonName() + "'")
                            .candidates(Collections.singletonList(CanonicalCandidateDto.builder()
                                    .id(season.getId())
                                    .name(season.getSeasonName())
                                    .reason("Canonical season " + season.getSeasonCode())
                                    .build()))
                            .build();
                }
            } else if (searchSeasons.size() > 1) {
                List<CanonicalCandidateDto> candidates = searchSeasons.stream()
                        .map(s -> CanonicalCandidateDto.builder()
                                .id(s.getId())
                                .name(s.getSeasonName())
                                .reason("Season code: " + s.getSeasonCode())
                                .build())
                        .toList();

                return buildAmbiguous(rawSeason, normalizedSeason, competition,
                        "Ambiguous: Multiple canonical seasons match representation '" + rawSeason + "'",
                        candidates);
            }
        }

        return SeasonResolutionResult.builder()
                .status(ResolutionStatus.UNRESOLVED)
                .originalInput(rawSeason != null ? rawSeason : (matchDate != null ? matchDate.toString() : null))
                .normalizedSeasonCode(rawSeason != null ? normalizationService.normalizeSeasonCode(rawSeason) : null)
                .competitionId(competition != null ? competition.getId() : null)
                .competitionName(competition != null ? competition.getName() : null)
                .explanation("Could not resolve season from incoming input")
                .candidates(Collections.emptyList())
                .build();
    }

    private SeasonResolutionResult buildResolved(
            String rawInput,
            String normalizedCode,
            Season season,
            CompetitionSeason cs,
            Competition comp,
            String explanation
    ) {
        return SeasonResolutionResult.builder()
                .status(ResolutionStatus.RESOLVED)
                .originalInput(rawInput)
                .normalizedSeasonCode(normalizedCode)
                .resolvedSeasonId(season.getId())
                .resolvedSeasonCode(season.getSeasonCode())
                .resolvedSeasonName(season.getSeasonName())
                .resolvedCompetitionSeasonId(cs != null ? cs.getId() : null)
                .competitionId(comp != null ? comp.getId() : (cs != null ? cs.getCompetition().getId() : null))
                .competitionName(comp != null ? comp.getName() : (cs != null ? cs.getCompetition().getName() : null))
                .startDate(cs != null ? cs.getStartDate() : season.getStartDate())
                .endDate(cs != null ? cs.getEndDate() : season.getEndDate())
                .explanation(explanation)
                .candidates(Collections.singletonList(CanonicalCandidateDto.builder()
                        .id(season.getId())
                        .name(season.getSeasonName())
                        .reason(explanation)
                        .build()))
                .build();
    }

    private SeasonResolutionResult buildAmbiguous(
            String rawInput,
            String normalizedCode,
            Competition comp,
            String explanation,
            List<CanonicalCandidateDto> candidates
    ) {
        return SeasonResolutionResult.builder()
                .status(ResolutionStatus.AMBIGUOUS)
                .originalInput(rawInput)
                .normalizedSeasonCode(normalizedCode)
                .competitionId(comp != null ? comp.getId() : null)
                .competitionName(comp != null ? comp.getName() : null)
                .explanation(explanation)
                .candidates(candidates)
                .build();
    }

    private List<CanonicalCandidateDto> toCandidates(List<CompetitionSeason> compSeasons) {
        return compSeasons.stream()
                .map(cs -> CanonicalCandidateDto.builder()
                        .id(cs.getSeason().getId())
                        .name(cs.getSeason().getSeasonName() + " (" + cs.getStartDate() + " to " + cs.getEndDate() + ")")
                        .reason("Competition season ID " + cs.getId())
                        .build())
                .toList();
    }
}
