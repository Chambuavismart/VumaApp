# Ingestion Engine Architecture

## 1. Architectural Overview

The Phase 3 Ingestion Engine provides robust historical data ingestion from `FOOTBALL_DATA_CO_UK`. It handles both single-season CSV files and stacked multi-season CSV files.

```
                      +-----------------------------+
                      |   Incoming CSV Data Source  |
                      |  (Manual Upload / Download) |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |  RFC-4180 CSV Parsing & BOM |
                      |    (FootballDataCsvParser)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |   Source Classification     |
                      | (FootballDataSourceClassifier|
                      +--------------+--------------+
                                     |
             +-----------------------+-----------------------+
             |                                               |
             v                                               v
   [SEASON_SPECIFIC CSV]                          [STACKED_MULTI_SEASON CSV]
   Single competition-season                      Multi-season rows in one file
             |                                               |
             +-----------------------+-----------------------+
                                     |
                                     v
                      +-----------------------------+
                      | Pre-Persistence Season Group|
                      |  & Eligibility Evaluation   |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |  Canonical Identity Res.    |
                      |  (Team/Competition/Season)  |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |  Isolated Season-Specific   |
                      |       Import Batches        |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      | Idempotent Match Upsert &   |
                      |  Source Row Provenance Log  |
                      +-----------------------------+
```

## 2. Component Directory

| Component | Responsibility |
|---|---|
| `FootballDataCsvParser` | RFC-4180 compliant parsing, UTF-8 BOM stripping, quoted fields, header mapping |
| `FootballDataSourceClassifier` | Classifies sources as `SEASON_SPECIFIC`, `STACKED_MULTI_SEASON`, or `UNKNOWN_FORMAT` |
| `FootballDataEligibilityService` | Evaluates required/optional fields, resolves canonical identity, validates boundaries |
| `FootballDataIngestionService` | Orchestrates import group creation, season group splitting, batch execution, and deduplication |
| `FootballDataSourceCatalogService` | Provides verified official download endpoints for Football-Data.co.uk |
| `FootballDataIngestionController` | REST API endpoints under `/api/v1/ingestion/` |
| `IngestionCentreComponent` | Angular frontend with dark theme (#0a0e14, #e11d48), stacked preview, and quarantine retry |
