package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityAliasManagementService {

    private final TeamRepository teamRepository;
    private final TeamAliasRepository teamAliasRepository;
    private final CompetitionRepository competitionRepository;
    private final CompetitionAliasRepository competitionAliasRepository;
    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;
    private final IdentityNormalizationService normalizationService;

    // --- OVERVIEW ---
    @Transactional(readOnly = true)
    public IdentityOverviewDto getOverview() {
        long totalTeams = teamRepository.count();
        long totalCompetitions = competitionRepository.count();
        long totalSeasons = seasonRepository.count();
        long totalCompSeasons = competitionSeasonRepository.count();
        long totalTeamAliases = teamAliasRepository.count();
        long totalCompAliases = competitionAliasRepository.count();

        Map<String, Long> teamAliasesBySource = new HashMap<>();
        for (Object[] row : teamAliasRepository.countBySourceGrouped()) {
            teamAliasesBySource.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Long> compAliasesBySource = new HashMap<>();
        for (Object[] row : competitionAliasRepository.countBySourceGrouped()) {
            compAliasesBySource.put((String) row[0], ((Number) row[1]).longValue());
        }

        return IdentityOverviewDto.builder()
                .totalCanonicalTeams(totalTeams)
                .totalCanonicalCompetitions(totalCompetitions)
                .totalSeasons(totalSeasons)
                .totalCompetitionSeasons(totalCompSeasons)
                .totalTeamAliases(totalTeamAliases)
                .totalCompetitionAliases(totalCompAliases)
                .teamAliasesBySource(teamAliasesBySource)
                .competitionAliasesBySource(compAliasesBySource)
                .build();
    }

    // --- TEAMS & TEAM ALIASES ---

    @Transactional(readOnly = true)
    public Page<TeamDetailDto> listTeams(String query, Pageable pageable) {
        Page<Team> teams;
        if (query != null && !query.trim().isEmpty()) {
            teams = teamRepository.searchTeams(query.trim(), pageable);
        } else {
            teams = teamRepository.findAll(pageable);
        }

        return teams.map(this::mapToTeamDetail);
    }

    @Transactional(readOnly = true)
    public Optional<TeamDetailDto> getTeam(Long teamId) {
        return teamRepository.findById(teamId).map(this::mapToTeamDetail);
    }

    @Transactional(readOnly = true)
    public List<TeamAliasDto> getTeamAliases(Long teamId) {
        return teamAliasRepository.findByTeamId(teamId).stream()
                .map(this::mapToTeamAliasDto)
                .toList();
    }

    @Transactional
    public TeamAliasDto registerTeamAlias(Long teamId, RegisterTeamAliasRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));

        String rawSource = request.getSource().trim();
        String normalizedSource = normalizationService.normalize(rawSource);
        String rawAlias = request.getRawAlias().trim();
        String normalizedAlias = normalizationService.normalize(rawAlias);

        String countryContext = request.getCountryContext() != null && !request.getCountryContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCountryContext())
                : null;
        String competitionContext = request.getCompetitionContext() != null && !request.getCompetitionContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCompetitionContext())
                : null;

        // Check for existing alias with identical scope
        Optional<TeamAlias> existing = teamAliasRepository.findScopedAlias(
                normalizedSource, normalizedAlias, countryContext, competitionContext);

        if (existing.isPresent()) {
            TeamAlias alias = existing.get();
            if (alias.getTeam().getId().equals(teamId)) {
                // Idempotent return
                log.info("Team alias already exists for team {} with identical scope: {}", teamId, normalizedAlias);
                return mapToTeamAliasDto(alias);
            } else {
                throw new IllegalStateException("Conflict: Alias '" + rawAlias + "' for provider '" + rawSource +
                        "' is already mapped to canonical team '" + alias.getTeam().getName() + "' (ID: " + alias.getTeam().getId() + ")");
            }
        }

        TeamAlias newAlias = TeamAlias.builder()
                .team(team)
                .source(normalizedSource)
                .rawAlias(rawAlias)
                .normalizedAlias(normalizedAlias)
                .countryContext(countryContext)
                .competitionContext(competitionContext)
                .active(true)
                .build();

        TeamAlias saved = teamAliasRepository.save(newAlias);
        log.info("Registered new team alias ID {} for team {} ('{}')", saved.getId(), team.getName(), rawAlias);
        return mapToTeamAliasDto(saved);
    }

    // --- COMPETITIONS & COMPETITION ALIASES ---

    @Transactional(readOnly = true)
    public Page<CompetitionDetailDto> listCompetitions(String query, Pageable pageable) {
        Page<Competition> comps;
        if (query != null && !query.trim().isEmpty()) {
            comps = competitionRepository.searchCompetitions(query.trim(), pageable);
        } else {
            comps = competitionRepository.findAll(pageable);
        }

        return comps.map(this::mapToCompetitionDetail);
    }

    @Transactional(readOnly = true)
    public Optional<CompetitionDetailDto> getCompetition(Long competitionId) {
        return competitionRepository.findById(competitionId).map(this::mapToCompetitionDetail);
    }

    @Transactional(readOnly = true)
    public List<CompetitionAliasDto> getCompetitionAliases(Long competitionId) {
        return competitionAliasRepository.findByCompetitionId(competitionId).stream()
                .map(this::mapToCompetitionAliasDto)
                .toList();
    }

    @Transactional
    public CompetitionAliasDto registerCompetitionAlias(Long competitionId, RegisterCompetitionAliasRequest request) {
        Competition comp = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found with ID: " + competitionId));

        String rawSource = request.getSource().trim();
        String normalizedSource = normalizationService.normalize(rawSource);
        String rawAlias = request.getRawAlias().trim();
        String normalizedAlias = normalizationService.normalizeCompetitionName(rawAlias);
        String sourceCode = request.getSourceCode() != null && !request.getSourceCode().trim().isEmpty()
                ? request.getSourceCode().trim()
                : null;
        String countryContext = request.getCountryContext() != null && !request.getCountryContext().trim().isEmpty()
                ? normalizationService.normalize(request.getCountryContext())
                : null;

        // Check for existing alias with identical scope
        Optional<CompetitionAlias> existing = competitionAliasRepository.findScopedAlias(
                normalizedSource, normalizedAlias, countryContext);

        if (existing.isPresent()) {
            CompetitionAlias alias = existing.get();
            if (alias.getCompetition().getId().equals(competitionId)) {
                log.info("Competition alias already exists for competition {} with identical scope: {}", competitionId, normalizedAlias);
                return mapToCompetitionAliasDto(alias);
            } else {
                throw new IllegalStateException("Conflict: Competition alias '" + rawAlias + "' for provider '" + rawSource +
                        "' is already mapped to canonical competition '" + alias.getCompetition().getName() + "' (ID: " + alias.getCompetition().getId() + ")");
            }
        }

        CompetitionAlias newAlias = CompetitionAlias.builder()
                .competition(comp)
                .source(normalizedSource)
                .sourceCode(sourceCode)
                .rawAlias(rawAlias)
                .normalizedAlias(normalizedAlias)
                .countryContext(countryContext)
                .active(true)
                .build();

        CompetitionAlias saved = competitionAliasRepository.save(newAlias);
        log.info("Registered new competition alias ID {} for competition {} ('{}')", saved.getId(), comp.getName(), rawAlias);
        return mapToCompetitionAliasDto(saved);
    }

    // --- SEASONS & COMPETITION SEASONS ---

    @Transactional(readOnly = true)
    public Page<SeasonDetailDto> listSeasons(String query, Pageable pageable) {
        Page<Season> seasons;
        if (query != null && !query.trim().isEmpty()) {
            seasons = seasonRepository.searchSeasons(query.trim(), pageable);
        } else {
            seasons = seasonRepository.findAll(pageable);
        }

        return seasons.map(this::mapToSeasonDetail);
    }

    @Transactional(readOnly = true)
    public Optional<SeasonDetailDto> getSeason(Long seasonId) {
        return seasonRepository.findById(seasonId).map(this::mapToSeasonDetail);
    }

    @Transactional(readOnly = true)
    public List<CompetitionSeasonDto> getCompetitionSeasons(Long competitionId) {
        return competitionSeasonRepository.findByCompetitionId(competitionId).stream()
                .map(this::mapToCompetitionSeasonDto)
                .toList();
    }

    // --- MAPPING HELPERS ---

    public TeamDetailDto mapToTeamDetail(Team team) {
        List<TeamAliasDto> aliases = teamAliasRepository.findByTeamId(team.getId()).stream()
                .map(this::mapToTeamAliasDto)
                .toList();

        return TeamDetailDto.builder()
                .id(team.getId())
                .name(team.getName())
                .normalizedName(team.getNormalizedName())
                .country(team.getCountry())
                .active(team.getActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .aliases(aliases)
                .build();
    }

    public TeamAliasDto mapToTeamAliasDto(TeamAlias a) {
        return TeamAliasDto.builder()
                .id(a.getId())
                .teamId(a.getTeam().getId())
                .teamName(a.getTeam().getName())
                .source(a.getSource())
                .rawAlias(a.getRawAlias())
                .normalizedAlias(a.getNormalizedAlias())
                .countryContext(a.getCountryContext())
                .competitionContext(a.getCompetitionContext())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public CompetitionDetailDto mapToCompetitionDetail(Competition c) {
        List<CompetitionAliasDto> aliases = competitionAliasRepository.findByCompetitionId(c.getId()).stream()
                .map(this::mapToCompetitionAliasDto)
                .toList();

        List<CompetitionSeasonDto> seasons = competitionSeasonRepository.findByCompetitionId(c.getId()).stream()
                .map(this::mapToCompetitionSeasonDto)
                .toList();

        return CompetitionDetailDto.builder()
                .id(c.getId())
                .name(c.getName())
                .normalizedName(c.getNormalizedName())
                .country(c.getCountry())
                .competitionType(c.getCompetitionType())
                .active(c.getActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .aliases(aliases)
                .seasons(seasons)
                .build();
    }

    public CompetitionAliasDto mapToCompetitionAliasDto(CompetitionAlias a) {
        return CompetitionAliasDto.builder()
                .id(a.getId())
                .competitionId(a.getCompetition().getId())
                .competitionName(a.getCompetition().getName())
                .source(a.getSource())
                .sourceCode(a.getSourceCode())
                .rawAlias(a.getRawAlias())
                .normalizedAlias(a.getNormalizedAlias())
                .countryContext(a.getCountryContext())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public CompetitionSeasonDto mapToCompetitionSeasonDto(CompetitionSeason cs) {
        return CompetitionSeasonDto.builder()
                .id(cs.getId())
                .competitionId(cs.getCompetition().getId())
                .competitionName(cs.getCompetition().getName())
                .seasonId(cs.getSeason().getId())
                .seasonCode(cs.getSeason().getSeasonCode())
                .seasonName(cs.getSeason().getSeasonName())
                .startDate(cs.getStartDate())
                .endDate(cs.getEndDate())
                .active(cs.getActive())
                .createdAt(cs.getCreatedAt())
                .updatedAt(cs.getUpdatedAt())
                .build();
    }

    public SeasonDetailDto mapToSeasonDetail(Season s) {
        List<CompetitionSeasonDto> compSeasons = competitionSeasonRepository.findBySeasonId(s.getId()).stream()
                .map(this::mapToCompetitionSeasonDto)
                .toList();

        return SeasonDetailDto.builder()
                .id(s.getId())
                .seasonCode(s.getSeasonCode())
                .seasonName(s.getSeasonName())
                .defaultStartDate(s.getStartDate())
                .defaultEndDate(s.getEndDate())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .competitionSeasons(compSeasons)
                .build();
    }
}
