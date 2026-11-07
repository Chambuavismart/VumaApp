package com.VumaPhaseTwo.service.ingestion;

import com.VumaPhaseTwo.dto.identity.ResolutionStatus;
import com.VumaPhaseTwo.dto.identity.TeamResolutionResult;
import com.VumaPhaseTwo.dto.ingestion.*;
import com.VumaPhaseTwo.model.*;
import com.VumaPhaseTwo.repository.*;
import com.VumaPhaseTwo.service.IdentityResolutionCoordinator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FootballDataIngestionService {

    private final FootballDataCsvParser csvParser;
    private final FootballDataSourceClassifier classifier;
    private final FootballDataEligibilityService eligibilityService;
    private final IdentityResolutionCoordinator identityCoordinator;

    private final ImportGroupRepository importGroupRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ImportQuarantineRecordRepository quarantineRecordRepository;
    private final MatchSourceRecordRepository matchSourceRecordRepository;
    private final HistoricalMatchRepository historicalMatchRepository;
    private final CompetitionSeasonRepository competitionSeasonRepository;
    private final CompetitionRepository competitionRepository;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CsvValidationReport validateAndPreview(
            String fileName,
            byte[] fileBytes,
            String suppliedSeasonContext,
            String suppliedCompetitionContext
    ) throws Exception {
        String checksum = calculateSha256(fileBytes);
        List<String> headers = csvParser.extractHeaders(new java.io.ByteArrayInputStream(fileBytes));
        List<ParsedCsvRow> rows = csvParser.parseCsv(new java.io.ByteArrayInputStream(fileBytes));

        var classification = classifier.classify(fileName, headers, rows, suppliedSeasonContext);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if ("UNKNOWN_FORMAT".equals(classification.format())) {
            errors.add(classification.reason());
        }

        // Group rows by season
        Map<String, List<ParsedCsvRow>> seasonGroups = new LinkedHashMap<>();

        if ("STACKED_MULTI_SEASON".equals(classification.format())) {
            for (ParsedCsvRow row : rows) {
                String s = row.getRawSeason() != null ? row.getRawSeason().trim() : "UNKNOWN_SEASON";
                seasonGroups.computeIfAbsent(s, k -> new ArrayList<>()).add(row);
            }
        } else {
            String seasonLabel = suppliedSeasonContext;
            if (seasonLabel == null || seasonLabel.trim().isEmpty()) {
                if (!classification.detectedSeasons().isEmpty()) {
                    seasonLabel = classification.detectedSeasons().iterator().next();
                } else {
                    seasonLabel = "UNSPECIFIED";
                }
            }
            seasonGroups.put(seasonLabel, rows);
        }

        List<SeasonEligibilitySummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<ParsedCsvRow>> entry : seasonGroups.entrySet()) {
            String seasonLabel = entry.getKey();
            List<ParsedCsvRow> seasonRows = entry.getValue();

            // Detect competition code from rows if not explicitly supplied
            String compCode = suppliedCompetitionContext;
            if (compCode == null || compCode.trim().isEmpty()) {
                for (ParsedCsvRow r : seasonRows) {
                    if (r.getDiv() != null && !r.getDiv().trim().isEmpty()) {
                        compCode = r.getDiv().trim();
                        break;
                    }
                }
            }

            SeasonEligibilitySummary summary = eligibilityService.evaluateSeasonGroup(seasonLabel, compCode, seasonRows);
            summaries.add(summary);
            if (!summary.isEligible()) {
                warnings.add("Season '" + seasonLabel + "' evaluated as ineligible: " + summary.getEligibilityReason());
            }
        }

        return CsvValidationReport.builder()
                .fileName(fileName)
                .fileChecksum(checksum)
                .fileSize(fileBytes.length)
                .distributionFormat(classification.format())
                .classificationReason(classification.reason())
                .valid(errors.isEmpty())
                .detectedHeaders(headers)
                .totalRows(rows.size())
                .hasSeasonColumn(classification.hasSeasonColumn())
                .seasonSummaries(summaries)
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    @Transactional
    public ImportGroupResponse createAndExecuteImport(
            String fileName,
            byte[] fileBytes,
            String sourceUrl,
            String suppliedSeasonContext,
            String suppliedCompetitionContext
    ) throws Exception {
        CsvValidationReport report = validateAndPreview(fileName, fileBytes, suppliedSeasonContext, suppliedCompetitionContext);
        if (!report.isValid()) {
            throw new IllegalArgumentException("Cannot import invalid CSV: " + String.join("; ", report.getErrors()));
        }

        List<ParsedCsvRow> allRows = csvParser.parseCsv(new java.io.ByteArrayInputStream(fileBytes));

        // Create ImportGroup (Physical file tracking)
        ImportGroup importGroup = ImportGroup.builder()
                .sourceName("FOOTBALL_DATA_CO_UK")
                .sourceFileName(fileName)
                .sourceFileChecksum(report.getFileChecksum())
                .sourceUrl(sourceUrl)
                .distributionFormat(report.getDistributionFormat())
                .status("PROCESSING")
                .totalPhysicalRows(allRows.size())
                .totalDetectedSeasons(report.getSeasonSummaries().size())
                .build();

        importGroup = importGroupRepository.save(importGroup);

        // Group rows by season
        Map<String, List<ParsedCsvRow>> seasonGroups = new LinkedHashMap<>();
        if ("STACKED_MULTI_SEASON".equals(report.getDistributionFormat())) {
            for (ParsedCsvRow row : allRows) {
                String s = row.getRawSeason() != null ? row.getRawSeason().trim() : "UNKNOWN_SEASON";
                seasonGroups.computeIfAbsent(s, k -> new ArrayList<>()).add(row);
            }
        } else {
            String seasonLabel = suppliedSeasonContext;
            if (seasonLabel == null || seasonLabel.trim().isEmpty()) {
                if (!report.getSeasonSummaries().isEmpty()) {
                    seasonLabel = report.getSeasonSummaries().get(0).getSuppliedSeasonLabel();
                } else {
                    seasonLabel = "UNSPECIFIED";
                }
            }
            seasonGroups.put(seasonLabel, allRows);
        }

        List<ImportBatch> createdBatches = new ArrayList<>();
        boolean anyBatchFailed = false;
        boolean anyBatchSuccess = false;

        for (SeasonEligibilitySummary summary : report.getSeasonSummaries()) {
            List<ParsedCsvRow> groupRows = seasonGroups.getOrDefault(summary.getSuppliedSeasonLabel(), Collections.emptyList());

            ImportBatch batch = processSeasonBatch(
                    importGroup,
                    fileName,
                    report.getFileChecksum(),
                    sourceUrl,
                    report.getDistributionFormat(),
                    summary,
                    groupRows
            );

            createdBatches.add(batch);
            if ("COMPLETED".equals(batch.getStatus())) {
                anyBatchSuccess = true;
            } else if ("FAILED".equals(batch.getStatus())) {
                anyBatchFailed = true;
            } else if ("PARTIAL_SUCCESS".equals(batch.getStatus())) {
                anyBatchSuccess = true;
                anyBatchFailed = true;
            }
        }

        if (anyBatchSuccess && !anyBatchFailed) {
            importGroup.setStatus("COMPLETED");
        } else if (anyBatchSuccess && anyBatchFailed) {
            importGroup.setStatus("PARTIAL_SUCCESS");
        } else {
            importGroup.setStatus("FAILED");
        }

        importGroup = importGroupRepository.save(importGroup);
        return mapToGroupResponse(importGroup, createdBatches);
    }

    private ImportBatch processSeasonBatch(
            ImportGroup importGroup,
            String fileName,
            String checksum,
            String sourceUrl,
            String distributionFormat,
            SeasonEligibilitySummary summary,
            List<ParsedCsvRow> rows
    ) {
        Competition comp = summary.getCanonicalCompetitionId() != null
                ? competitionRepository.findById(summary.getCanonicalCompetitionId()).orElse(null)
                : null;
        Season season = summary.getCanonicalSeasonId() != null
                ? seasonRepository.findById(summary.getCanonicalSeasonId()).orElse(null)
                : null;
        CompetitionSeason compSeason = summary.getCompetitionSeasonId() != null
                ? competitionSeasonRepository.findById(summary.getCompetitionSeasonId()).orElse(null)
                : null;

        ImportBatch batch = ImportBatch.builder()
                .importGroup(importGroup)
                .sourceName("FOOTBALL_DATA_CO_UK")
                .sourceFileName(fileName)
                .sourceFileChecksum(checksum)
                .sourceUrl(sourceUrl)
                .distributionFormat(distributionFormat)
                .competition(comp)
                .season(season)
                .competitionSeason(compSeason)
                .importType("HISTORICAL_CSV")
                .status("PROCESSING")
                .totalRows(rows.size())
                .successfulRows(0)
                .insertedRows(0)
                .updatedRows(0)
                .skippedRows(0)
                .quarantinedRows(0)
                .failedRows(0)
                .startedAt(LocalDateTime.now())
                .build();

        batch = importBatchRepository.save(batch);

        if (!summary.isEligible() || comp == null || season == null || compSeason == null) {
            // Quarantine entire group if ineligible or missing canonical relationship
            for (ParsedCsvRow row : rows) {
                quarantineRow(batch, fileName, row.getOriginalRowNumber(), "INELIGIBLE_SEASON_GROUP",
                        summary.getEligibilityReason(), row.getRawValues());
            }
            batch.setStatus("FAILED");
            batch.setQuarantinedRows(rows.size());
            batch.setCompletedAt(LocalDateTime.now());
            batch.setErrorSummary("Batch ineligible: " + summary.getEligibilityReason());
            return importBatchRepository.save(batch);
        }

        int inserted = 0;
        int skipped = 0;
        int quarantined = 0;

        for (ParsedCsvRow row : rows) {
            try {
                // Team Resolution
                TeamResolutionResult homeTeamRes = identityCoordinator.resolveTeam(
                        row.getHomeTeam(),
                        "FOOTBALL_DATA_CO_UK",
                        comp.getCountry(),
                        comp.getName()
                );

                TeamResolutionResult awayTeamRes = identityCoordinator.resolveTeam(
                        row.getAwayTeam(),
                        "FOOTBALL_DATA_CO_UK",
                        comp.getCountry(),
                        comp.getName()
                );

                if (homeTeamRes.getStatus() != ResolutionStatus.RESOLVED || awayTeamRes.getStatus() != ResolutionStatus.RESOLVED) {
                    String reason = "Unresolved team identity: Home=" + row.getHomeTeam() + " (" + homeTeamRes.getStatus() + "), Away=" + row.getAwayTeam() + " (" + awayTeamRes.getStatus() + ")";
                    quarantineRow(batch, fileName, row.getOriginalRowNumber(), "IDENTITY_UNRESOLVED", reason, row.getRawValues());
                    quarantined++;
                    continue;
                }

                Team homeTeam = teamRepository.findById(homeTeamRes.getResolvedTeamId()).orElse(null);
                Team awayTeam = teamRepository.findById(awayTeamRes.getResolvedTeamId()).orElse(null);

                if (homeTeam == null || awayTeam == null) {
                    String reason = "Canonical team record missing from database";
                    quarantineRow(batch, fileName, row.getOriginalRowNumber(), "IDENTITY_UNRESOLVED", reason, row.getRawValues());
                    quarantined++;
                    continue;
                }

                if (homeTeam.getId().equals(awayTeam.getId())) {
                    quarantineRow(batch, fileName, row.getOriginalRowNumber(), "INVALID_MATCH_DATA", "Home and away teams resolve to the same canonical entity", row.getRawValues());
                    quarantined++;
                    continue;
                }

                LocalDate matchDate = eligibilityService.parseDate(row.getDateStr());
                if (matchDate == null) {
                    quarantineRow(batch, fileName, row.getOriginalRowNumber(), "INVALID_DATE", "Cannot parse date: " + row.getDateStr(), row.getRawValues());
                    quarantined++;
                    continue;
                }

                if (row.getFthg() == null || row.getFtag() == null) {
                    quarantineRow(batch, fileName, row.getOriginalRowNumber(), "MISSING_SCORES", "Missing full-time home/away goals", row.getRawValues());
                    quarantined++;
                    continue;
                }

                // Natural key
                String naturalKey = generateNaturalMatchKey(comp.getId(), season.getId(), matchDate, homeTeam.getId(), awayTeam.getId());

                // Check duplicate match
                Optional<HistoricalMatch> existingMatchOpt = historicalMatchRepository.findByNaturalMatchKey(naturalKey);
                HistoricalMatch match;
                boolean isNew = false;

                if (existingMatchOpt.isPresent()) {
                    match = existingMatchOpt.get();
                    skipped++;
                } else {
                    match = HistoricalMatch.builder()
                            .competition(comp)
                            .season(season)
                            .competitionSeason(compSeason)
                            .matchDate(matchDate)
                            .homeTeam(homeTeam)
                            .awayTeam(awayTeam)
                            .homeGoals(row.getFthg())
                            .awayGoals(row.getFtag())
                            .fullTimeResult(row.getFtr() != null ? row.getFtr() : calculateFtr(row.getFthg(), row.getFtag()))
                            .halfTimeHomeGoals(row.getHthg())
                            .halfTimeAwayGoals(row.getHtag())
                            .halfTimeResult(row.getHtr())
                            .homeShots(row.getHs())
                            .awayShots(row.getAs())
                            .homeShotsOnTarget(row.getHst())
                            .awayShotsOnTarget(row.getAst())
                            .homeCorners(row.getHc())
                            .awayCorners(row.getAc())
                            .homeFouls(row.getHf())
                            .awayFouls(row.getAf())
                            .homeYellowCards(row.getHy())
                            .awayYellowCards(row.getAy())
                            .homeRedCards(row.getHr())
                            .awayRedCards(row.getAr())
                            .over25Odds(row.getAvgOver25Odds() != null ? BigDecimal.valueOf(row.getAvgOver25Odds()) : null)
                            .under25Odds(row.getAvgUnder25Odds() != null ? BigDecimal.valueOf(row.getAvgUnder25Odds()) : null)
                            .naturalMatchKey(naturalKey)
                            .build();

                    match = historicalMatchRepository.save(match);
                    inserted++;
                    isNew = true;
                }

                // Match Source Record Provenance
                String rawDataJson = objectMapper.writeValueAsString(row.getRawValues());
                String sourceRecordHash = calculateSha256((fileName + "_" + row.getOriginalRowNumber() + "_" + rawDataJson).getBytes());

                Optional<MatchSourceRecord> existingSrcOpt = matchSourceRecordRepository.findBySourceRecordHash(sourceRecordHash);
                if (existingSrcOpt.isEmpty()) {
                    MatchSourceRecord sourceRecord = MatchSourceRecord.builder()
                            .historicalMatch(match)
                            .importBatch(batch)
                            .sourceName("FOOTBALL_DATA_CO_UK")
                            .sourceFileName(fileName)
                            .sourceRowNumber(row.getOriginalRowNumber())
                            .sourceRecordHash(sourceRecordHash)
                            .build();

                    matchSourceRecordRepository.save(sourceRecord);
                }

            } catch (Exception e) {
                log.error("Error processing row {}: {}", row.getOriginalRowNumber(), e.getMessage(), e);
                quarantineRow(batch, fileName, row.getOriginalRowNumber(), "PROCESSING_ERROR", e.getMessage(), row.getRawValues());
                quarantined++;
            }
        }

        batch.setInsertedRows(inserted);
        batch.setSkippedRows(skipped);
        batch.setQuarantinedRows(quarantined);
        batch.setSuccessfulRows(inserted + skipped);
        batch.setCompletedAt(LocalDateTime.now());

        if (quarantined == 0) {
            batch.setStatus("COMPLETED");
        } else if (inserted > 0 || skipped > 0) {
            batch.setStatus("PARTIAL_SUCCESS");
        } else {
            batch.setStatus("FAILED");
        }

        return importBatchRepository.save(batch);
    }

    private void quarantineRow(
            ImportBatch batch,
            String fileName,
            int rowNumber,
            String category,
            String reason,
            Map<String, String> rawValues
    ) {
        try {
            String rawJson = rawValues != null ? objectMapper.writeValueAsString(rawValues) : "{}";
            ImportQuarantineRecord record = ImportQuarantineRecord.builder()
                    .importBatch(batch)
                    .sourceFileName(fileName)
                    .sourceRowNumber(rowNumber)
                    .failureCategory(category)
                    .failureReason(reason)
                    .rawRowData(rawJson)
                    .resolutionStatus("UNRESOLVED")
                    .build();

            quarantineRecordRepository.save(record);
        } catch (Exception e) {
            log.error("Failed to save quarantine record for row {}: {}", rowNumber, e.getMessage());
        }
    }

    public IngestionDashboardStats getDashboardStats() {
        long totalGroups = importGroupRepository.count();
        long totalBatches = importBatchRepository.count();
        long completedBatches = importBatchRepository.findAll().stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();
        long failedBatches = importBatchRepository.findAll().stream().filter(b -> "FAILED".equals(b.getStatus())).count();
        long partialBatches = importBatchRepository.findAll().stream().filter(b -> "PARTIAL_SUCCESS".equals(b.getStatus())).count();
        long pendingBatches = importBatchRepository.findAll().stream().filter(b -> "PENDING".equals(b.getStatus()) || "PROCESSING".equals(b.getStatus())).count();

        long stackedGroups = importGroupRepository.findAll().stream().filter(g -> "STACKED_MULTI_SEASON".equals(g.getDistributionFormat())).count();
        long singleGroups = importGroupRepository.findAll().stream().filter(g -> "SEASON_SPECIFIC".equals(g.getDistributionFormat())).count();

        long totalImported = historicalMatchRepository.count();
        long totalQuarantined = quarantineRecordRepository.count();

        long totalSkipped = importBatchRepository.findAll().stream().mapToLong(b -> b.getSkippedRows() != null ? b.getSkippedRows() : 0).sum();
        long totalFailed = importBatchRepository.findAll().stream().mapToLong(b -> b.getFailedRows() != null ? b.getFailedRows() : 0).sum();

        return IngestionDashboardStats.builder()
                .totalPhysicalFilesReceived(totalGroups)
                .seasonSpecificFilesCount(singleGroups)
                .stackedFilesCount(stackedGroups)
                .totalImportGroups(totalGroups)
                .totalSeasonBatches(totalBatches)
                .completedBatches(completedBatches)
                .failedBatches(failedBatches)
                .partialSuccessBatches(partialBatches)
                .pendingOrProcessingBatches(pendingBatches)
                .totalImportedMatches(totalImported)
                .totalDuplicateMatches(totalSkipped)
                .totalQuarantinedRecords(totalQuarantined)
                .totalFailedRecords(totalFailed)
                .build();
    }

    public List<ImportGroupResponse> listImportGroups() {
        List<ImportGroup> groups = importGroupRepository.findAllByOrderByCreatedAtDesc();
        List<ImportGroupResponse> responses = new ArrayList<>();
        for (ImportGroup g : groups) {
            List<ImportBatch> batches = importBatchRepository.findByImportGroupId(g.getId());
            responses.add(mapToGroupResponse(g, batches));
        }
        return responses;
    }

    public Optional<ImportGroupResponse> getGroupDetails(Long groupId) {
        return importGroupRepository.findById(groupId).map(g -> {
            List<ImportBatch> batches = importBatchRepository.findByImportGroupId(g.getId());
            return mapToGroupResponse(g, batches);
        });
    }

    public List<ImportBatchSummaryResponse> listBatches() {
        return importBatchRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToBatchSummary)
                .toList();
    }

    public Optional<ImportBatchSummaryResponse> getBatchDetails(Long batchId) {
        return importBatchRepository.findById(batchId).map(this::mapToBatchSummary);
    }

    public List<QuarantineRecordResponse> listQuarantineRecords(Long batchId, String status) {
        List<ImportQuarantineRecord> records;
        if (batchId != null) {
            records = quarantineRecordRepository.findByImportBatchId(batchId);
        } else if (status != null && !status.trim().isEmpty()) {
            records = quarantineRecordRepository.findByResolutionStatus(status.trim());
        } else {
            records = quarantineRecordRepository.findAllByOrderByCreatedAtDesc();
        }

        return records.stream().map(r -> QuarantineRecordResponse.builder()
                .id(r.getId())
                .importBatchId(r.getImportBatch() != null ? r.getImportBatch().getId() : null)
                .sourceFileName(r.getSourceFileName())
                .sourceRowNumber(r.getSourceRowNumber())
                .failureCategory(r.getFailureCategory())
                .failureReason(r.getFailureReason())
                .rawData(r.getRawRowData())
                .resolutionStatus(r.getResolutionStatus())
                .resolvedAt(r.getResolvedAt() != null ? r.getResolvedAt().toString() : null)
                .notes(null)
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .build()
        ).toList();
    }

    @Transactional
    public boolean retryQuarantinedRecord(Long quarantineId) {
        Optional<ImportQuarantineRecord> opt = quarantineRecordRepository.findById(quarantineId);
        if (opt.isEmpty()) return false;
        ImportQuarantineRecord q = opt.get();
        if ("RESOLVED".equals(q.getResolutionStatus())) return true;

        try {
            // Attempt to re-parse raw data
            @SuppressWarnings("unchecked")
            Map<String, String> raw = objectMapper.readValue(q.getRawRowData(), Map.class);
            ImportBatch batch = q.getImportBatch();
            if (batch == null || batch.getCompetition() == null || batch.getSeason() == null || batch.getCompetitionSeason() == null) {
                return false;
            }

            String homeTeamStr = raw.get("HomeTeam") != null ? raw.get("HomeTeam") : raw.get("Home");
            String awayTeamStr = raw.get("AwayTeam") != null ? raw.get("AwayTeam") : raw.get("Away");
            String dateStr = raw.get("Date");
            String fthgStr = raw.get("FTHG") != null ? raw.get("FTHG") : raw.get("HG");
            String ftagStr = raw.get("FTAG") != null ? raw.get("FTAG") : raw.get("AG");
            String ftrStr = raw.get("FTR") != null ? raw.get("FTR") : raw.get("Res");

            TeamResolutionResult homeTeamRes = identityCoordinator.resolveTeam(
                    homeTeamStr,
                    "FOOTBALL_DATA_CO_UK",
                    batch.getCompetition().getCountry(),
                    batch.getCompetition().getName()
            );

            TeamResolutionResult awayTeamRes = identityCoordinator.resolveTeam(
                    awayTeamStr,
                    "FOOTBALL_DATA_CO_UK",
                    batch.getCompetition().getCountry(),
                    batch.getCompetition().getName()
            );

            if (homeTeamRes.getStatus() == ResolutionStatus.RESOLVED && awayTeamRes.getStatus() == ResolutionStatus.RESOLVED) {
                Team homeTeam = teamRepository.findById(homeTeamRes.getResolvedTeamId()).orElse(null);
                Team awayTeam = teamRepository.findById(awayTeamRes.getResolvedTeamId()).orElse(null);
                LocalDate matchDate = eligibilityService.parseDate(dateStr);
                Integer fthg = fthgStr != null ? Integer.parseInt(fthgStr) : null;
                Integer ftag = ftagStr != null ? Integer.parseInt(ftagStr) : null;

                if (homeTeam != null && awayTeam != null && matchDate != null && fthg != null && ftag != null) {
                    String naturalKey = generateNaturalMatchKey(batch.getCompetition().getId(), batch.getSeason().getId(), matchDate, homeTeam.getId(), awayTeam.getId());
                    HistoricalMatch match = historicalMatchRepository.findByNaturalMatchKey(naturalKey).orElseGet(() ->
                            historicalMatchRepository.save(HistoricalMatch.builder()
                                    .competition(batch.getCompetition())
                                    .season(batch.getSeason())
                                    .competitionSeason(batch.getCompetitionSeason())
                                    .matchDate(matchDate)
                                    .homeTeam(homeTeam)
                                    .awayTeam(awayTeam)
                                    .homeGoals(fthg)
                                    .awayGoals(ftag)
                                    .fullTimeResult(ftrStr != null ? ftrStr : calculateFtr(fthg, ftag))
                                    .naturalMatchKey(naturalKey)
                                    .build())
                    );

                    q.setResolutionStatus("RESOLVED");
                    q.setResolvedAt(LocalDateTime.now());
                    quarantineRecordRepository.save(q);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Retry failed for quarantine ID {}: {}", quarantineId, e.getMessage());
        }
        return false;
    }

    private ImportGroupResponse mapToGroupResponse(ImportGroup g, List<ImportBatch> batches) {
        List<ImportBatchSummaryResponse> batchDtos = batches != null
                ? batches.stream().map(this::mapToBatchSummary).toList()
                : Collections.emptyList();

        return ImportGroupResponse.builder()
                .id(g.getId())
                .sourceName(g.getSourceName())
                .sourceFileName(g.getSourceFileName())
                .sourceFileChecksum(g.getSourceFileChecksum())
                .sourceUrl(g.getSourceUrl())
                .distributionFormat(g.getDistributionFormat())
                .status(g.getStatus())
                .totalPhysicalRows(g.getTotalPhysicalRows())
                .totalDetectedSeasons(g.getTotalDetectedSeasons())
                .createdAt(g.getCreatedAt() != null ? g.getCreatedAt().toString() : null)
                .updatedAt(g.getUpdatedAt() != null ? g.getUpdatedAt().toString() : null)
                .batches(batchDtos)
                .build();
    }

    private ImportBatchSummaryResponse mapToBatchSummary(ImportBatch b) {
        return ImportBatchSummaryResponse.builder()
                .id(b.getId())
                .importGroupId(b.getImportGroup() != null ? b.getImportGroup().getId() : null)
                .sourceName(b.getSourceName())
                .sourceFileName(b.getSourceFileName())
                .sourceFileChecksum(b.getSourceFileChecksum())
                .distributionFormat(b.getDistributionFormat())
                .competitionId(b.getCompetition() != null ? b.getCompetition().getId() : null)
                .competitionName(b.getCompetition() != null ? b.getCompetition().getName() : null)
                .seasonId(b.getSeason() != null ? b.getSeason().getId() : null)
                .seasonName(b.getSeason() != null ? b.getSeason().getSeasonName() : null)
                .seasonCode(b.getSeason() != null ? b.getSeason().getSeasonCode() : null)
                .competitionSeasonId(b.getCompetitionSeason() != null ? b.getCompetitionSeason().getId() : null)
                .status(b.getStatus())
                .totalRows(b.getTotalRows())
                .successfulRows(b.getSuccessfulRows())
                .insertedRows(b.getInsertedRows())
                .updatedRows(b.getUpdatedRows())
                .skippedRows(b.getSkippedRows())
                .quarantinedRows(b.getQuarantinedRows())
                .failedRows(b.getFailedRows())
                .startedAt(b.getStartedAt() != null ? b.getStartedAt().toString() : null)
                .completedAt(b.getCompletedAt() != null ? b.getCompletedAt().toString() : null)
                .errorSummary(b.getErrorSummary())
                .build();
    }

    private String generateNaturalMatchKey(Long compId, Long seasonId, LocalDate date, Long homeId, Long awayId) {
        return "C" + compId + "_S" + seasonId + "_" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + "_H" + homeId + "_A" + awayId;
    }

    private String calculateFtr(int hg, int ag) {
        if (hg > ag) return "H";
        if (ag > hg) return "A";
        return "D";
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 computation failed", e);
        }
    }
}
