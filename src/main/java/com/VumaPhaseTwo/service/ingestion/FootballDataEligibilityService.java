package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.identity.CompetitionResolutionResult;
import com.VumaPhaseTwo.dto.identity.ResolutionStatus;
import com.VumaPhaseTwo.dto.identity.SeasonResolutionResult;
import com.VumaPhaseTwo.dto.ingestion.ParsedCsvRow;
import com.VumaPhaseTwo.dto.ingestion.SeasonEligibilitySummary;
import com.VumaPhaseTwo.model.Competition;
import com.VumaPhaseTwo.model.CompetitionSeason;
import com.VumaPhaseTwo.model.Season;
import com.VumaPhaseTwo.repository.CompetitionRepository;
import com.VumaPhaseTwo.repository.CompetitionSeasonRepository;
import com.VumaPhaseTwo.repository.SeasonRepository;
import com.VumaPhaseTwo.service.IdentityResolutionCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootballDataEligibilityService {

    private final IdentityResolutionCoordinator identityCoordinator;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    public SeasonEligibilitySummary evaluateSeasonGroup(
            String rawSeasonLabel,
            String competitionCodeOrName,
            List<ParsedCsvRow> rows
    ) {
        List<String> validationErrors = new ArrayList<>();
        List<String> requiredFields = List.of("Date", "HomeTeam", "AwayTeam", "FTHG", "FTAG", "FTR");
        List<String> availableRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> availableOptional = new ArrayList<>();

        if (rows == null || rows.isEmpty()) {
            return SeasonEligibilitySummary.builder()
                    .suppliedSeasonLabel(rawSeasonLabel)
                    .detectedRowCount(0)
                    .eligible(false)
                    .eligibilityReason("No match rows detected for season")
                    .validationErrors(List.of("Empty season record set"))
                    .build();
        }

        // Check required and optional metrics across rows
        boolean hasDate = true, hasHome = true, hasAway = true, hasFthg = true, hasFtag = true, hasFtr = true;
        boolean hasHt = false, hasShots = false, hasCorners = false, hasFouls = false, hasCards = false, hasOdds = false;

        for (ParsedCsvRow r : rows) {
            if (r.getDateStr() == null) hasDate = false;
            if (r.getHomeTeam() == null) hasHome = false;
            if (r.getAwayTeam() == null) hasAway = false;
            if (r.getFthg() == null) hasFthg = false;
            if (r.getFtag() == null) hasFtag = false;
            if (r.getFtr() == null) hasFtr = false;

            if (r.getHthg() != null && r.getHtag() != null) hasHt = true;
            if (r.getHs() != null && r.getAs() != null) hasShots = true;
            if (r.getHc() != null && r.getAc() != null) hasCorners = true;
            if (r.getHf() != null && r.getAf() != null) hasFouls = true;
            if (r.getHy() != null && r.getAy() != null) hasCards = true;
            if (r.getAvgOver25Odds() != null || r.getAvgUnder25Odds() != null) hasOdds = true;
        }

        if (hasDate) availableRequired.add("Date"); else missingRequired.add("Date");
        if (hasHome) availableRequired.add("HomeTeam"); else missingRequired.add("HomeTeam");
        if (hasAway) availableRequired.add("AwayTeam"); else missingRequired.add("AwayTeam");
        if (hasFthg) availableRequired.add("FTHG"); else missingRequired.add("FTHG");
        if (hasFtag) availableRequired.add("FTAG"); else missingRequired.add("FTAG");
        if (hasFtr) availableRequired.add("FTR"); else missingRequired.add("FTR");

        if (hasHt) availableOptional.add("HalfTimeScores");
        if (hasShots) availableOptional.add("ShotsAndShotsOnTarget");
        if (hasCorners) availableOptional.add("Corners");
        if (hasFouls) availableOptional.add("Fouls");
        if (hasCards) availableOptional.add("Cards");
        if (hasOdds) availableOptional.add("OverUnder2.5Odds");

        // Resolve Competition
        Long canonicalCompId = null;
        String canonicalCompName = null;
        Competition canonicalCompetition = null;

        if (competitionCodeOrName != null && !competitionCodeOrName.trim().isEmpty()) {
            CompetitionResolutionResult compRes = identityCoordinator.resolveCompetition(
                    competitionCodeOrName.trim(),
                    "FOOTBALL_DATA_CO_UK",
                    null
            );
            if (compRes.getStatus() == ResolutionStatus.RESOLVED) {
                canonicalCompId = compRes.getResolvedCompetitionId();
                canonicalCompName = compRes.getResolvedCompetitionName();
                canonicalCompetition = competitionRepository.findById(canonicalCompId).orElse(null);
            } else {
                validationErrors.add("Unable to resolve canonical competition for: " + competitionCodeOrName + " (" + compRes.getStatus() + ")");
            }
        }

        // Resolve Season
        Long canonicalSeasonId = null;
        String canonicalSeasonCode = null;
        String canonicalSeasonName = null;
        Season canonicalSeason = null;

        if (rawSeasonLabel != null && !rawSeasonLabel.trim().isEmpty()) {
            SeasonResolutionResult seasonRes = identityCoordinator.resolveSeason(
                    rawSeasonLabel.trim(),
                    canonicalCompId,
                    canonicalCompName,
                    null
            );
            if (seasonRes.getStatus() == ResolutionStatus.RESOLVED) {
                canonicalSeasonId = seasonRes.getResolvedSeasonId();
                canonicalSeasonName = seasonRes.getResolvedSeasonName();
                canonicalSeasonCode = seasonRes.getResolvedSeasonCode();
                canonicalSeason = seasonRepository.findById(canonicalSeasonId).orElse(null);
            } else {
                validationErrors.add("Unable to resolve canonical season for label '" + rawSeasonLabel + "' (" + seasonRes.getStatus() + ")");
            }
        } else {
            validationErrors.add("Missing season label for group");
        }

        // Competition Season relationship and boundaries
        Long compSeasonId = null;
        if (canonicalCompetition != null && canonicalSeason != null) {
            Optional<CompetitionSeason> csOpt = competitionSeasonRepository.findByCompetitionIdAndSeasonId(
                    canonicalCompetition.getId(),
                    canonicalSeason.getId()
            );
            if (csOpt.isPresent()) {
                CompetitionSeason cs = csOpt.get();
                compSeasonId = cs.getId();

                // Validate dates against boundary
                LocalDate startDate = cs.getStartDate();
                LocalDate endDate = cs.getEndDate();

                int boundaryMismatchCount = 0;
                for (ParsedCsvRow r : rows) {
                    LocalDate matchDate = parseDate(r.getDateStr());
                    if (matchDate != null) {
                        if (startDate != null && matchDate.isBefore(startDate)) {
                            boundaryMismatchCount++;
                        } else if (endDate != null && matchDate.isAfter(endDate)) {
                            boundaryMismatchCount++;
                        }
                    }
                }

                if (boundaryMismatchCount > 0) {
                    validationErrors.add(boundaryMismatchCount + " match dates fall outside competition-season boundary (" + startDate + " to " + endDate + ")");
                }
            } else {
                validationErrors.add("CompetitionSeason mapping not found for Competition ID " + canonicalCompetition.getId() + " and Season ID " + canonicalSeason.getId());
            }
        }

        // Quality rule: Season < 2016/2017 should not be imported if missing core metrics
        boolean isHistoricalExcluded = false;
        if (canonicalSeasonCode != null) {
            try {
                int startYear = Integer.parseInt(canonicalSeasonCode.split("-|/|_")[0]);
                if (startYear < 2016 && !hasShots && !hasOdds) {
                    isHistoricalExcluded = true;
                    validationErrors.add("Historical season (" + canonicalSeasonCode + ") lacks required match metrics (shots/odds) per quality policy");
                }
            } catch (Exception ignored) {
            }
        }

        boolean eligible = missingRequired.isEmpty() && validationErrors.isEmpty() && !isHistoricalExcluded;
        String eligibilityReason = eligible
                ? "Eligible for season-specific import (" + rows.size() + " matches, full required fields present)"
                : "Ineligible: " + String.join("; ", validationErrors);

        return SeasonEligibilitySummary.builder()
                .suppliedSeasonLabel(rawSeasonLabel)
                .canonicalSeasonId(canonicalSeasonId)
                .canonicalSeasonCode(canonicalSeasonCode)
                .canonicalSeasonName(canonicalSeasonName)
                .canonicalCompetitionId(canonicalCompId)
                .canonicalCompetitionName(canonicalCompName)
                .competitionSeasonId(compSeasonId)
                .detectedRowCount(rows.size())
                .availableRequiredFields(availableRequired)
                .availableOptionalFields(availableOptional)
                .missingRequiredFields(missingRequired)
                .eligible(eligible)
                .eligibilityReason(eligibilityReason)
                .validationErrors(validationErrors)
                .build();
    }

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String cleaned = dateStr.trim();
        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, dtf);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
