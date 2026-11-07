package com.VumaPhaseTwo.controller;

import com.VumaPhaseTwo.dto.DatabaseDtos.*;
import com.VumaPhaseTwo.service.DatabaseInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/database")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:8080", "http://127.0.0.1:8080"}, allowCredentials = "true")
public class DatabaseInspectionController {

    private final DatabaseInspectionService databaseInspectionService;

    @GetMapping("/overview")
    public ResponseEntity<OverviewResponse> getOverview() {
        return ResponseEntity.ok(databaseInspectionService.getDatabaseOverview());
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableSummaryResponse>> getTables() {
        return ResponseEntity.ok(databaseInspectionService.getTableSummaries());
    }

    @GetMapping("/tables/{tableName}")
    public ResponseEntity<TableStructureResponse> getTableStructure(@PathVariable String tableName) {
        return ResponseEntity.ok(databaseInspectionService.getTableStructure(tableName));
    }

    @GetMapping("/relationships")
    public ResponseEntity<List<RelationshipResponse>> getRelationships() {
        return ResponseEntity.ok(databaseInspectionService.getRelationships());
    }

    @GetMapping("/migrations")
    public ResponseEntity<List<MigrationRecordResponse>> getMigrations() {
        return ResponseEntity.ok(databaseInspectionService.getMigrationHistory());
    }
}
