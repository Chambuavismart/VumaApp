package com.VumaPhaseTwo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Identity Normalization Service Tests")
class IdentityNormalizationServiceTest {

    private IdentityNormalizationService normalizationService;

    @BeforeEach
    void setUp() {
        normalizationService = new IdentityNormalizationService();
    }

    @Test
    @DisplayName("Should normalize whitespace, casing, and basic accents")
    void testBasicNormalization() {
        assertEquals("arsenal", normalizationService.normalize("  Arsenal  "));
        assertEquals("arsenal", normalizationService.normalize("ARSENAL"));
        assertEquals("arsenal", normalizationService.normalize("Arsenal   \t\n"));
        assertEquals("atletico madrid", normalizationService.normalize("Atlético Madrid"));
        assertEquals("bayern munchen", normalizationService.normalize("Bayern München"));
        assertEquals("inter milan", normalizationService.normalize("Inter   Milan"));
    }

    @Test
    @DisplayName("Should normalize apostrophes and hyphens consistently")
    void testApostrophesAndHyphens() {
        assertEquals("brighton & hove albion", normalizationService.normalize("Brighton & Hove Albion"));
        assertEquals("newell's old boys", normalizationService.normalize("Newell’s Old Boys"));
        assertEquals("newell's old boys", normalizationService.normalize("Newell`s Old Boys"));
        assertEquals("paris saint-germain", normalizationService.normalize("Paris Saint–Germain"));
        assertEquals("paris saint-germain", normalizationService.normalize("Paris Saint—Germain"));
    }

    @Test
    @DisplayName("Should remove punctuation noise")
    void testPunctuationRemoval() {
        assertEquals("arsenal f c", normalizationService.normalize("Arsenal F.C."));
        assertEquals("tottenham hotspur thfc", normalizationService.normalize("Tottenham Hotspur (THFC)"));
        assertEquals("a s roma", normalizationService.normalize("A.S. Roma"));
    }

    @Test
    @DisplayName("Should strip common harmless team suffixes in normalizeTeamName")
    void testTeamSuffixNormalization() {
        assertEquals("arsenal", normalizationService.normalizeTeamName("Arsenal FC"));
        assertEquals("arsenal", normalizationService.normalizeTeamName("Arsenal F.C."));
        assertEquals("arsenal", normalizationService.normalizeTeamName("Arsenal Football Club"));
        assertEquals("chelsea", normalizationService.normalizeTeamName("Chelsea FC"));
        assertEquals("liverpool", normalizationService.normalizeTeamName("Liverpool AFC"));
        assertEquals("roma", normalizationService.normalizeTeamName("AS Roma"));
        assertEquals("roma", normalizationService.normalizeTeamName("Roma Calcio"));
        // Short team names that are words by themselves should not be decimated
        assertEquals("sc heerenveen", normalizationService.normalize("SC Heerenveen"));
    }

    @Test
    @DisplayName("Should normalize various season formats to standard representations")
    void testSeasonCodeNormalization() {
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("2024/25"));
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("2024/2025"));
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("2024-25"));
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("2024-2025"));
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("  2024 / 2025 "));
        assertEquals("2024/2025", normalizationService.normalizeSeasonCode("24/25"));
        assertEquals("2024", normalizationService.normalizeSeasonCode("2024"));
        assertEquals("2024", normalizationService.normalizeSeasonCode("  2024  "));
    }

    @Test
    @DisplayName("Should handle empty and null inputs safely")
    void testNullAndEmptyInputs() {
        assertEquals("", normalizationService.normalize(null));
        assertEquals("", normalizationService.normalize("   "));
        assertEquals("", normalizationService.normalizeTeamName(null));
        assertEquals("", normalizationService.normalizeTeamName("   "));
        assertEquals("", normalizationService.normalizeSeasonCode(null));
        assertEquals("", normalizationService.normalizeSeasonCode("   "));
    }
}
