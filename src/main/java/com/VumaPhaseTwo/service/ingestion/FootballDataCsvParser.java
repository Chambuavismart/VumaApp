package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.ingestion.ParsedCsvRow;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FootballDataCsvParser {

    public List<String> extractHeaders(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return extractHeaders(new ByteArrayInputStream(bytes), true);
    }

    public List<String> extractHeaders(InputStream inputStream, boolean stripBom) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return Collections.emptyList();
        }
        if (stripBom && headerLine.startsWith("\uFEFF")) {
            headerLine = headerLine.substring(1);
        }

        List<String> headers = parseCsvLine(headerLine);
        List<String> result = new ArrayList<>();
        for (String h : headers) {
            result.add(h != null ? h.trim() : "");
        }
        return result;
    }

    public List<ParsedCsvRow> parseCsv(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        List<List<String>> allLines = parseCsvContent(content);
        if (allLines.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> headerList = allLines.get(0);
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerList.size(); i++) {
            String h = headerList.get(i);
            if (h != null && !h.trim().isEmpty()) {
                headerMap.put(h.trim().toUpperCase(Locale.ROOT), i);
            }
        }

        List<ParsedCsvRow> rows = new ArrayList<>();
        for (int i = 1; i < allLines.size(); i++) {
            List<String> line = allLines.get(i);
            if (line == null || line.isEmpty() || (line.size() == 1 && line.get(0).trim().isEmpty())) {
                continue;
            }

            // Check if entire row is empty
            boolean hasData = false;
            for (String colVal : line) {
                if (colVal != null && !colVal.trim().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (!hasData) {
                continue;
            }

            Map<String, String> rawMap = new LinkedHashMap<>();
            for (int col = 0; col < headerList.size() && col < line.size(); col++) {
                String h = headerList.get(col) != null ? headerList.get(col).trim() : "COL_" + col;
                rawMap.put(h, line.get(col) != null ? line.get(col).trim() : "");
            }

            ParsedCsvRow row = ParsedCsvRow.builder()
                    .originalRowNumber(i + 1)
                    .rawSeason(getStringVal(line, headerMap, "SEASON"))
                    .div(getStringVal(line, headerMap, "DIV"))
                    .dateStr(getStringVal(line, headerMap, "DATE"))
                    .timeStr(getStringVal(line, headerMap, "TIME"))
                    .homeTeam(getStringVal(line, headerMap, "HOMETEAM", "HOME"))
                    .awayTeam(getStringVal(line, headerMap, "AWAYTEAM", "AWAY"))
                    .fthg(getIntVal(line, headerMap, "FTHG", "HG"))
                    .ftag(getIntVal(line, headerMap, "FTAG", "AG"))
                    .ftr(getStringVal(line, headerMap, "FTR", "RES"))
                    .hthg(getIntVal(line, headerMap, "HTHG"))
                    .htag(getIntVal(line, headerMap, "HTAG"))
                    .htr(getStringVal(line, headerMap, "HTR"))
                    .referee(getStringVal(line, headerMap, "REFEREE"))
                    .hs(getIntVal(line, headerMap, "HS"))
                    .as(getIntVal(line, headerMap, "AS"))
                    .hst(getIntVal(line, headerMap, "HST"))
                    .ast(getIntVal(line, headerMap, "AST"))
                    .hc(getIntVal(line, headerMap, "HC"))
                    .ac(getIntVal(line, headerMap, "AC"))
                    .hf(getIntVal(line, headerMap, "HF"))
                    .af(getIntVal(line, headerMap, "AF"))
                    .hy(getIntVal(line, headerMap, "HY"))
                    .ay(getIntVal(line, headerMap, "AY"))
                    .hr(getIntVal(line, headerMap, "HR"))
                    .ar(getIntVal(line, headerMap, "AR"))
                    .avgOver25Odds(getDoubleVal(line, headerMap, "Avg>2.5", "AVG>2.5", "B365>2.5", "BbAv>2.5"))
                    .avgUnder25Odds(getDoubleVal(line, headerMap, "Avg<2.5", "AVG<2.5", "B365<2.5", "BbAv<2.5"))
                    .maxOver25Odds(getDoubleVal(line, headerMap, "Max>2.5", "MAX>2.5", "BbMx>2.5"))
                    .maxUnder25Odds(getDoubleVal(line, headerMap, "Max<2.5", "MAX<2.5", "BbMx<2.5"))
                    .rawValues(rawMap)
                    .build();

            rows.add(row);
        }

        return rows;
    }

    public List<List<String>> parseCsvContent(String content) {
        List<List<String>> records = new ArrayList<>();
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    currentRecord.add(currentField.toString().trim());
                    currentField.setLength(0);
                } else if (c == '\r') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                        i++;
                    }
                    currentRecord.add(currentField.toString().trim());
                    currentField.setLength(0);
                    records.add(new ArrayList<>(currentRecord));
                    currentRecord.clear();
                } else if (c == '\n') {
                    currentRecord.add(currentField.toString().trim());
                    currentField.setLength(0);
                    records.add(new ArrayList<>(currentRecord));
                    currentRecord.clear();
                } else {
                    currentField.append(c);
                }
            }
        }

        if (currentField.length() > 0 || !currentRecord.isEmpty()) {
            currentRecord.add(currentField.toString().trim());
            records.add(currentRecord);
        }

        return records;
    }

    public List<String> parseCsvLine(String line) {
        List<List<String>> records = parseCsvContent(line);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.get(0);
    }

    private String getStringVal(List<String> line, Map<String, Integer> headerMap, String... candidates) {
        for (String c : candidates) {
            Integer idx = headerMap.get(c.toUpperCase(Locale.ROOT));
            if (idx != null && idx < line.size()) {
                String val = line.get(idx);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private Integer getIntVal(List<String> line, Map<String, Integer> headerMap, String... candidates) {
        String val = getStringVal(line, headerMap, candidates);
        if (val == null) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double getDoubleVal(List<String> line, Map<String, Integer> headerMap, String... candidates) {
        String val = getStringVal(line, headerMap, candidates);
        if (val == null) {
            return null;
        }
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
