package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.identity.*;
import com.VumaPhaseTwo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityResolutionController {

    private final TeamIdentityResolutionService teamResolutionService;
    private final CompetitionIdentityResolutionService competitionResolutionService;
    private final SeasonIdentityResolutionService seasonResolutionService;
    private final IdentityAliasManagementService aliasManagementService;

    // --- OVERVIEW ---
    @GetMapping("/overview")
    public ResponseEntity<IdentityOverviewDto> getOverview() {
        return ResponseEntity.ok(aliasManagementService.getOverview());
    }

    // --- TEAMS & TEAM ALIASES ---

    @PostMapping("/teams/resolve")
    public ResponseEntity<TeamResolutionResult> resolveTeam(@RequestBody TeamResolutionRequest request) {
        return ResponseEntity.ok(teamResolutionService.resolve(request));
    }

    @GetMapping("/teams")
    public ResponseEntity<Page<TeamDetailDto>> listTeams(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(aliasManagementService.listTeams(query, pageable));
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamDetailDto> getTeam(@PathVariable Long teamId) {
        return aliasManagementService.getTeam(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teams/{teamId}/aliases")
    public ResponseEntity<List<TeamAliasDto>> getTeamAliases(@PathVariable Long teamId) {
        return ResponseEntity.ok(aliasManagementService.getTeamAliases(teamId));
    }

    @PostMapping("/teams/{teamId}/aliases")
    public ResponseEntity<?> registerTeamAlias(
            @PathVariable Long teamId,
            @Valid @RequestBody RegisterTeamAliasRequest request
    ) {
        try {
            TeamAliasDto created = aliasManagementService.registerTeamAlias(teamId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // --- COMPETITIONS & COMPETITION ALIASES ---

    @PostMapping("/competitions/resolve")
    public ResponseEntity<CompetitionResolutionResult> resolveCompetition(@RequestBody CompetitionResolutionRequest request) {
        return ResponseEntity.ok(competitionResolutionService.resolve(request));
    }

    @GetMapping("/competitions")
    public ResponseEntity<Page<CompetitionDetailDto>> listCompetitions(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(aliasManagementService.listCompetitions(query, pageable));
    }

    @GetMapping("/competitions/{competitionId}")
    public ResponseEntity<CompetitionDetailDto> getCompetition(@PathVariable Long competitionId) {
        return aliasManagementService.getCompetition(competitionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/competitions/{competitionId}/aliases")
    public ResponseEntity<List<CompetitionAliasDto>> getCompetitionAliases(@PathVariable Long competitionId) {
        return ResponseEntity.ok(aliasManagementService.getCompetitionAliases(competitionId));
    }

    @PostMapping("/competitions/{competitionId}/aliases")
    public ResponseEntity<?> registerCompetitionAlias(
            @PathVariable Long competitionId,
            @Valid @RequestBody RegisterCompetitionAliasRequest request
    ) {
        try {
            CompetitionAliasDto created = aliasManagementService.registerCompetitionAlias(competitionId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/competitions/{competitionId}/seasons")
    public ResponseEntity<List<CompetitionSeasonDto>> getCompetitionSeasons(@PathVariable Long competitionId) {
        return ResponseEntity.ok(aliasManagementService.getCompetitionSeasons(competitionId));
    }

    // --- SEASONS ---

    @PostMapping("/seasons/resolve")
    public ResponseEntity<SeasonResolutionResult> resolveSeason(@RequestBody SeasonResolutionRequest request) {
        return ResponseEntity.ok(seasonResolutionService.resolve(request));
    }

    @GetMapping("/seasons")
    public ResponseEntity<Page<SeasonDetailDto>> listSeasons(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "seasonCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(aliasManagementService.listSeasons(query, pageable));
    }

    @GetMapping("/seasons/{seasonId}")
    public ResponseEntity<SeasonDetailDto> getSeason(@PathVariable Long seasonId) {
        return aliasManagementService.getSeason(seasonId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
