package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.ingestion.ParsedCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballDataCsvParserAndClassifierTest {

    private FootballDataCsvParser parser;
    private FootballDataSourceClassifier classifier;

    @BeforeEach
    void setUp() {
        parser = new FootballDataCsvParser();
        classifier = new FootballDataSourceClassifier();
    }

    @Test
    void testExtractHeadersWithUtf8Bom() throws Exception {
        String csvContent = "\uFEFFDiv,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR\r\nE0,12/08/2023,Arsenal,Nott'm Forest,2,1,H\r\n";
        List<String> headers = parser.extractHeaders(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

        assertEquals(7, headers.size());
        assertEquals("Div", headers.get(0));
        assertEquals("HomeTeam", headers.get(2));
    }

    @Test
    void testParseCsvWithQuotedFieldsAndCommas() throws Exception {
        String csvContent = "Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR\r\nE0,12/08/2023,\"Brighton and Hove Albion\",Luton,4,1,H\r\n";
        List<ParsedCsvRow> rows = parser.parseCsv(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, rows.size());
        ParsedCsvRow row = rows.get(0);
        assertEquals("Brighton and Hove Albion", row.getHomeTeam());
        assertEquals("Luton", row.getAwayTeam());
        assertEquals(4, row.getFthg());
        assertEquals(1, row.getFtag());
        assertEquals("H", row.getFtr());
    }

    @Test
    void testClassifySingleSeasonFile() throws Exception {
        String csvContent = "Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR\r\nE0,12/08/2023,Arsenal,Nott'm Forest,2,1,H\r\n";
        List<String> headers = parser.extractHeaders(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
        List<ParsedCsvRow> rows = parser.parseCsv(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

        var res = classifier.classify("E0.csv", headers, rows, "2023/2024");
        assertEquals("SEASON_SPECIFIC", res.format());
        assertFalse(res.hasSeasonColumn());
    }

    @Test
    void testClassifyStackedMultiSeasonFileWithSeasonColumn() throws Exception {
        String csvContent = "Season,Div,Date,HomeTeam,AwayTeam,FTHG,FTAG,FTR\r\n2022/2023,E0,12/08/2022,Arsenal,Leicester,4,2,H\r\n2023/2024,E0,12/08/2023,Arsenal,Nott'm Forest,2,1,H\r\n";
        List<String> headers = parser.extractHeaders(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
        List<ParsedCsvRow> rows = parser.parseCsv(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

        var res = classifier.classify("E0_all.csv", headers, rows, null);
        assertEquals("STACKED_MULTI_SEASON", res.format());
        assertTrue(res.hasSeasonColumn());
        assertEquals(2, res.detectedSeasons().size());
        assertTrue(res.detectedSeasons().contains("2022/2023"));
        assertTrue(res.detectedSeasons().contains("2023/2024"));
    }

    @Test
    void testClassifyUnknownFormatWhenCoreColumnsMissing() throws Exception {
        String csvContent = "ColA,ColB,ColC\r\n1,2,3\r\n";
        List<String> headers = parser.extractHeaders(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
        List<ParsedCsvRow> rows = parser.parseCsv(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

        var res = classifier.classify("invalid.csv", headers, rows, null);
        assertEquals("UNKNOWN_FORMAT", res.format());
    }
}
