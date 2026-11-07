# Phase 3 Implementation Report
## Historical Football Data Ingestion, Stacked-CSV Separation, and Season-Specific Import Engine

**Project:** VumaPhaseTwo  
**Implementation Phase:** Phase 3  
**Status:** Completed & Fully Verified  
**Date:** November 2026  

---

### Executive Summary

Phase 3 delivers an enterprise-grade Historical Football Data Ingestion Engine for `FOOTBALL_DATA_CO_UK`. The central architectural achievement of this phase is **strict season-specific database ingestion and isolation**, guaranteeing that even when physical source files are distributed as stacked multi-season CSVs, records are systematically classified, parsed, split, validated, and persisted into strictly season-isolated logical import batches (`import_batches`) linked to their parent source file (`import_groups`).

---

### Key Architectural Deliverables

1. **Source Classification Engine (`FootballDataSourceClassifier`):**
   - Classifies every incoming CSV file into `SEASON_SPECIFIC`, `STACKED_MULTI_SEASON`, or `UNKNOWN_FORMAT`.
   - Never relies solely on file names; inspects headers, row contents, detected season identifiers, and supplied execution context.

2. **RFC-4180 Compliant CSV Parser (`FootballDataCsvParser`):**
   - Supports UTF-8 and UTF-8 with BOM (`\uFEFF`).
   - Handles quoted fields with internal commas and escaped quotes (`""`).
   - Parses CRLF and LF line endings, empty cells, unexpected additional columns, and variable column orders.
   - Extracts full-time and half-time metrics, disciplinary records, shots, corners, fouls, and over/under 2.5 betting market odds.

3. **Stacked Multi-Season Separation & Pre-Persistence Grouping (`FootballDataIngestionService`):**
   - Groups match rows by normalized season identity before persistence.
   - Creates distinct logical `ImportBatch` records for each detected season, referencing the physical `ImportGroup`.
   - Enforces the strict invariant: **Every logical batch contains exactly one canonical `competition_season_id`.**

4. **Season Identity Normalization & Boundary Verification (`FootballDataEligibilityService`):**
   - Integrates directly with Phase 2 canonical Season & Competition Identity Resolution engines.
   - Preserves calendar-year seasons (e.g., `2025` is never coerced into `2025/2026`).
   - Normalizes European dual-year seasons (`2024/25`, `2024/2025`).
   - Validates match dates against canonical competition-season start and end dates.

5. **Data Eligibility & Metric Quality Policy:**
   - Evaluates mandatory vs. optional fields per season group before ingestion.
   - Missing scores or odds are safely stored as `NULL` (never fabricated as zero).
   - Enforces data-quality policy: historical seasons prior to 2016/2017 lacking required metrics (shots/odds) are quarantined with clear explanations rather than polluting the historical dataset.

6. **Idempotent Ingestion & Source Provenance:**
   - Backed by deterministic natural match keys: `C{compId}_S{seasonId}_{date}_H{homeId}_A{awayId}`.
   - Re-importing identical files avoids duplicate match creation and increments `skippedRows`.
   - Preserves original physical file name, file SHA-256 checksum, and original CSV row numbers in `match_source_records`.

7. **Quarantine Management & Resolution Workflow:**
   - Unresolved team aliases, date boundary mismatches, or schema errors are quarantined into `import_quarantine_records` with raw JSON row data.
   - Supports one-click retry once aliases are registered in the Identity Management Centre.

8. **Angular Ingestion Centre (Dark Theme #0a0e14, #e11d48):**
   - Real-time Metrics Dashboard showing physical files, stacked vs single files, season batches, imported matches, and quarantine counts.
   - CSV Ingestion & Stacked Multi-Season Preview tab detailing detected seasons, required/optional fields, and eligibility status before execution.
   - Provider Download Catalogue with verified official `football-data.co.uk` endpoints.
   - Batch Execution History & Lineage table.
   - Quarantine Manager with retry triggers.

---

### Test Suite Execution Summary

- **Total Unit & Integration Tests:** 66
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Angular Production Build:** Passed (0 errors, standalone architecture)

---

### Acceptance Checklist

- [x] Season-specific CSV ingestion works.
- [x] Stacked multi-season CSV ingestion works.
- [x] Stacked files are separated into individual season-specific logical batches.
- [x] Every batch has exactly one canonical competition-season.
- [x] Original file and row provenance are preserved.
- [x] Calendar-year seasons remain correctly identified.
- [x] Competition-specific season boundaries are respected.
- [x] Provider URLs are verified rather than guessed.
- [x] Data eligibility is evaluated per season.
- [x] Identity resolution is integrated.
- [x] Invalid and unresolved rows are quarantined.
- [x] Duplicate imports are safe and idempotent.
- [x] Batch processing is recoverable.
- [x] Angular interface works and builds cleanly.
- [x] Automated tests pass (66/66).
- [x] Live verification completed.
