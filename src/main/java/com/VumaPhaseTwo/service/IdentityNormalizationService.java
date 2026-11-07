package com.VumaPhaseTwo.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Service responsible for deterministic string normalization across teams,
 * competitions, seasons, countries, and sources.
 */
@Service
public class IdentityNormalizationService {

    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern UNICODE_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern SPECIAL_APOSTROPHES = Pattern.compile("['`´ʼ’‘]");
    private static final Pattern HYPHENS_AND_DASHES = Pattern.compile("[–—―−]");
    private static final Pattern PUNCTUATION_TO_REMOVE = Pattern.compile("[.,;:!?()\"{}\\[\\]_#*^~]");

    // Common harmless suffixes for team names (when isolated as words at the end)
    private static final Pattern TEAM_SUFFIXES = Pattern.compile(
            "\\b(f\\s*c|a\\s*f\\s*c|fc|afc|sc|cf|ac|sv|sk|fk|bk|if|ff|ss|as|us|cd|ca|sd|ud|rb|bv|vfb|vfl|tsv|bsc|spvg|spvgg|calcio|football club)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Standard normalization for names/aliases (teams, competitions, countries, sources).
     *
     * @param input Raw input string
     * @return Normalized, trimmed, lowercase string with normalized punctuation and diacritics.
     */
    public String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // 1. Unicode NFD normalization and stripping diacritics (e.g. Atlético -> Atletico, Bayern München -> Bayern Munchen)
        String decomposed = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        String withoutDiacritics = UNICODE_DIACRITICS.matcher(decomposed).replaceAll("");

        // 2. Normalize apostrophes and hyphens
        String normalizedApos = SPECIAL_APOSTROPHES.matcher(withoutDiacritics).replaceAll("'");
        String normalizedDashes = HYPHENS_AND_DASHES.matcher(normalizedApos).replaceAll("-");

        // 3. Remove punctuation like dots, commas, parentheses
        String cleanPunctuation = PUNCTUATION_TO_REMOVE.matcher(normalizedDashes).replaceAll(" ");

        // 4. Normalize casing to lowercase (Root Locale)
        String lower = cleanPunctuation.toLowerCase(Locale.ROOT);

        // 5. Replace multiple whitespace with single space and trim
        String result = MULTI_WHITESPACE.matcher(lower).replaceAll(" ").trim();

        return result;
    }

    /**
     * Normalized name for teams, with optional harmless suffix stripping when needed.
     * Retains core name while providing predictable representation.
     */
    public String normalizeTeamName(String input) {
        String base = normalize(input);
        if (base.isEmpty()) {
            return "";
        }

        // Clean up common suffix noise if present at the end of the team name (e.g. "Arsenal FC" -> "arsenal")
        // But only when not reducing the name to nothing
        String stripped = TEAM_SUFFIXES.matcher(base).replaceAll(" ");
        String cleaned = MULTI_WHITESPACE.matcher(stripped).replaceAll(" ").trim();

        return cleaned.isEmpty() ? base : cleaned;
    }

    /**
     * Normalizes competition names/codes.
     */
    public String normalizeCompetitionName(String input) {
        return normalize(input);
    }

    /**
     * Normalizes season representations like "2024/25", "2024/2025", "2024-25", "2024-2025", "2024".
     * Standardizes cross-year seasons into "YYYY/YYYY" or "YYYY" for calendar seasons.
     */
    public String normalizeSeasonCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String raw = input.trim().replaceAll("\\s+", "");

        // Pattern: 2024/2025 or 2024-2025
        if (raw.matches("^(\\d{4})[/\\-](\\d{4})$")) {
            String[] parts = raw.split("[/\\-]");
            return parts[0] + "/" + parts[1];
        }

        // Pattern: 2024/25 or 2024-25
        if (raw.matches("^(\\d{4})[/\\-](\\d{2})$")) {
            String[] parts = raw.split("[/\\-]");
            int startYear = Integer.parseInt(parts[0]);
            int century = startYear / 100;
            int endYearShort = Integer.parseInt(parts[1]);
            int fullEndYear = century * 100 + endYearShort;
            return startYear + "/" + fullEndYear;
        }

        // Pattern: 24/25 or 24-25 (short format sometimes used)
        if (raw.matches("^(\\d{2})[/\\-](\\d{2})$")) {
            String[] parts = raw.split("[/\\-]");
            int startYear = 2000 + Integer.parseInt(parts[0]); // assume 2000s
            int endYear = 2000 + Integer.parseInt(parts[1]);
            return startYear + "/" + endYear;
        }

        // Single year format: e.g. "2024"
        if (raw.matches("^\\d{4}$")) {
            return raw;
        }

        // Fallback to lowercase trimmed string
        return normalize(input);
    }
}
