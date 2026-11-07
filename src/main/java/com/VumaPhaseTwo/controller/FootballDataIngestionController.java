package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.ingestion.*;
import com.VumaPhaseTwo.service.ingestion.FootballDataIngestionService;
import com.VumaPhaseTwo.service.ingestion.FootballDataSourceCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
@Slf4j
public class FootballDataIngestionController {

    private final FootballDataIngestionService ingestionService;
    private final FootballDataSourceCatalogService catalogService;

    @GetMapping("/catalog")
    public ResponseEntity<List<FootballDataSourceOption>> getDownloadCatalog() {
        return ResponseEntity.ok(catalogService.getVerifiedSources());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<IngestionDashboardStats> getDashboardStats() {
        return ResponseEntity.ok(ingestionService.getDashboardStats());
    }

    @PostMapping("/validate")
    public ResponseEntity<CsvValidationReport> validateCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "seasonContext", required = false) String seasonContext,
            @RequestParam(value = "competitionContext", required = false) String competitionContext
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            CsvValidationReport report = ingestionService.validateAndPreview(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    seasonContext,
                    competitionContext
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("CSV validation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ImportGroupResponse> uploadAndImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "seasonContext", required = false) String seasonContext,
            @RequestParam(value = "competitionContext", required = false) String competitionContext
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            ImportGroupResponse response = ingestionService.createAndExecuteImport(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    null,
                    seasonContext,
                    competitionContext
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("CSV import execution failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/download-and-import")
    public ResponseEntity<ImportGroupResponse> downloadAndImport(
            @RequestParam("downloadUrl") String downloadUrl,
            @RequestParam(value = "competitionContext", required = false) String competitionContext,
            @RequestParam(value = "seasonContext", required = false) String seasonContext
    ) {
        try {
            // Security check: validate domain is football-data.co.uk
            URI uri = URI.create(downloadUrl);
            if (uri.getHost() == null || !uri.getHost().toLowerCase().endsWith("football-data.co.uk")) {
                return ResponseEntity.badRequest().build();
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "VumaPhaseTwo-Ingestion-Engine/1.0")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                log.warn("Download failed for URL {} with status {}", downloadUrl, response.statusCode());
                return ResponseEntity.status(response.statusCode()).build();
            }

            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || !fileName.endsWith(".csv")) {
                fileName = "downloaded_football_data.csv";
            }

            ImportGroupResponse importResponse = ingestionService.createAndExecuteImport(
                    fileName,
                    response.body(),
                    downloadUrl,
                    seasonContext,
                    competitionContext
            );

            return ResponseEntity.ok(importResponse);
        } catch (Exception e) {
            log.error("Download and import failed for {}: {}", downloadUrl, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<List<ImportGroupResponse>> listImportGroups() {
        return ResponseEntity.ok(ingestionService.listImportGroups());
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ImportGroupResponse> getGroupDetails(@PathVariable Long groupId) {
        return ingestionService.getGroupDetails(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batches")
    public ResponseEntity<List<ImportBatchSummaryResponse>> listBatches() {
        return ResponseEntity.ok(ingestionService.listBatches());
    }

    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ImportBatchSummaryResponse> getBatchDetails(@PathVariable Long batchId) {
        return ingestionService.getBatchDetails(batchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quarantine")
    public ResponseEntity<List<QuarantineRecordResponse>> listQuarantineRecords(
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return ResponseEntity.ok(ingestionService.listQuarantineRecords(batchId, status));
    }

    @PostMapping("/quarantine/{quarantineId}/retry")
    public ResponseEntity<Void> retryQuarantine(@PathVariable Long quarantineId) {
        boolean success = ingestionService.retryQuarantinedRecord(quarantineId);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
