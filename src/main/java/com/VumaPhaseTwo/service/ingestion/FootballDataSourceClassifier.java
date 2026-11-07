package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.ingestion.ParsedCsvRow;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FootballDataSourceClassifier {

    public ClassificationResult classify(String fileName, List<String> headers, List<ParsedCsvRow> rows, String suppliedSeasonContext) {
        if (headers == null || headers.isEmpty()) {
            return new ClassificationResult("UNKNOWN_FORMAT", "File contains no headers or is empty", Collections.emptySet(), false);
        }

        Set<String> headerSet = new HashSet<>();
        for (String h : headers) {
            if (h != null) {
                headerSet.add(h.trim().toUpperCase(Locale.ROOT));
            }
        }

        // Basic check for football data headers
        boolean hasTeams = (headerSet.contains("HOMETEAM") || headerSet.contains("HOME"))
                && (headerSet.contains("AWAYTEAM") || headerSet.contains("AWAY"));
        boolean hasDate = headerSet.contains("DATE");

        if (!hasTeams || !hasDate) {
            return new ClassificationResult("UNKNOWN_FORMAT",
                    "Missing core match headers (Date, HomeTeam, AwayTeam). Detected: " + headers,
                    Collections.emptySet(), false);
        }

        boolean hasSeasonCol = headerSet.contains("SEASON");
        Set<String> distinctSeasonsInRows = new LinkedHashSet<>();

        for (ParsedCsvRow row : rows) {
            if (row.getRawSeason() != null && !row.getRawSeason().trim().isEmpty()) {
                distinctSeasonsInRows.add(row.getRawSeason().trim());
            }
        }

        // Rule 1: If Season column exists and contains > 1 distinct season values -> STACKED_MULTI_SEASON
        if (hasSeasonCol && distinctSeasonsInRows.size() > 1) {
            return new ClassificationResult("STACKED_MULTI_SEASON",
                    "CSV contains 'Season' column with multiple distinct season values: " + distinctSeasonsInRows,
                    distinctSeasonsInRows, true);
        }

        // Rule 2: If Season column exists with exactly 1 distinct season value
        if (hasSeasonCol && distinctSeasonsInRows.size() == 1) {
            return new ClassificationResult("SEASON_SPECIFIC",
                    "CSV contains 'Season' column with single season value: " + distinctSeasonsInRows.iterator().next(),
                    distinctSeasonsInRows, true);
        }

        // Rule 3: If no Season column, but known stacked file naming pattern or explicit stacked indicator
        if (fileName != null && (fileName.toLowerCase(Locale.ROOT).contains("all") || fileName.toLowerCase(Locale.ROOT).contains("stacked"))) {
            if (!hasSeasonCol || distinctSeasonsInRows.isEmpty()) {
                return new ClassificationResult("UNKNOWN_FORMAT",
                        "File appears to be stacked by name/context but lacks a reliable row-level 'Season' column",
                        distinctSeasonsInRows, false);
            }
        }

        // Rule 4: No Season column, typical single-season file
        return new ClassificationResult("SEASON_SPECIFIC",
                "CSV does not contain 'Season' column and represents a single season dataset (season context required or supplied: " + (suppliedSeasonContext != null ? suppliedSeasonContext : "NONE") + ")",
                distinctSeasonsInRows, false);
    }

    public record ClassificationResult(
            String format,
            String reason,
            Set<String> detectedSeasons,
            boolean hasSeasonColumn
    ) {}
}
