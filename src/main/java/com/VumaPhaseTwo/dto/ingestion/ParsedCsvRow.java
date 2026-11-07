package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedCsvRow {
    private int originalRowNumber;
    private String rawSeason;
    private String div;
    private String dateStr;
    private String timeStr;
    private String homeTeam;
    private String awayTeam;
    private Integer fthg;
    private Integer ftag;
    private String ftr;
    private Integer hthg;
    private Integer htag;
    private String htr;
    private String referee;
    private Integer hs;
    private Integer as;
    private Integer hst;
    private Integer ast;
    private Integer hc;
    private Integer ac;
    private Integer hf;
    private Integer af;
    private Integer hy;
    private Integer ay;
    private Integer hr;
    private Integer ar;
    private Double avgOver25Odds;
    private Double avgUnder25Odds;
    private Double maxOver25Odds;
    private Double maxUnder25Odds;
    private Map<String, String> rawValues;
}
