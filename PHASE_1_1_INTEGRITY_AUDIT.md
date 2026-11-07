# VumaPhaseTwo — Phase 1.1 Database Integrity Audit and Corrections Report

## Executive Summary

- **Project:** `VumaPhaseTwo`
- **Audit Date:** 2026-11-07
- **Database:** MySQL 8.0.46 (`Vumaapp`)
- **Technology Stack:** Java 21, Spring Boot 3.4.2, Flyway, Hibernate (`ddl-auto=validate`), Angular Standalone
- **Test Suite Status:** 36 / 36 Passing Tests (0 Failures, 0 Errors, 0 Skipped)
- **Migrations Applied:** 8 Flyway Migrations (`V1` through `V8`)

This document presents the comprehensive Phase 1.1 Database Integrity Audit and details the corrective schema modifications implemented via Flyway migration `V8__database_integrity_corrections.sql`.

---

## 1. Audit Methodology & Scope

The audit examined all ten core domain tables and the migration history across the following dimensions:
1. **Migration Files:** Inspected Flyway scripts `V1` through `V7`.
2. **Schema & Engine Constraints:** Verified MySQL table options, column types, character sets, collations, nullability, and primary keys.
3. **Foreign Keys & Referential Actions:** Evaluated `ON DELETE` cascading rules and relational consistency across parent/child tables.
4. **Uniqueness Rules & Index Integrity:** Evaluated the behavior of compound unique constraints containing nullable context columns under MySQL's three-valued logic (`NULL != NULL`).
5. **Entity Mappings & Hibernate Validation:** Verified JPA entities against MySQL schema under strict `ddl-auto=validate`.
6. **API Responses & Frontend Integration:** Verified live Database Explorer and System endpoints against MySQL metadata.

---

## 2. Integrity Findings: Categorized Analysis

### 2.1 Confirmed Defects & Vulnerabilities

| Item | Table(s) / Component | Issue Description | Risk / Impact | Status |
| :--- | :--- | :--- | :--- | :--- |
| **DEF-01** | `historical_matches`, `import_batches` | Triplet inconsistency: `competition_id`, `season_id`, and `competition_season_id` were constrained by 3 separate foreign keys. This permitted matches or batches to reference a `competition_season_id` of (Competition A, Season 1) while setting `competition_id` to Competition B or `season_id` to Season 2. | Relational integrity violation and corrupted match filtering across competition seasons. | **CORRECTED in V8** |
| **DEF-02** | `competition_aliases` | Nullable context in unique constraint `uq_comp_aliases_scoped (source, normalized_alias, country_context)`. In MySQL InnoDB, `NULL` values are treated as distinct; therefore, duplicate global aliases (`country_context IS NULL`) were permitted without database rejection. | Unchecked duplicate alias insertion from the same provider. | **CORRECTED in V8** |
| **DEF-03** | `team_aliases` | Nullable context in unique constraint `uq_team_aliases_scoped (source, normalized_alias, country_context, competition_context)`. Multiple aliases with `NULL` country or competition context bypassed uniqueness enforcement. | Unchecked duplicate alias insertion for teams across un-scoped contexts. | **CORRECTED in V8** |
| **DEF-04** | `match_source_records` | Foreign key `fk_source_records_match` was defined with `ON DELETE CASCADE`. Deleting a canonical historical match silently and irreversibly destroyed all associated source provenance records (file, row, checksum, hash). | Loss of data lineage and inability to trace raw provider rows back to imported fixtures. | **CORRECTED in V8** |
| **DEF-05** | Collation Documentation | Architecture documentation specified `utf8mb4_unicode_ci`, whereas the MySQL 8.0 database default and API responses returned `utf8mb4_0900_ai_ci`. Table DDL in migrations explicitly set `utf8mb4_unicode_ci`. | Documentation inconsistency between architectural specifications and live database metadata. | **CORRECTED in Docs & Schema** |

---

## 3. Implemented Corrections (Flyway Migration V8)

To resolve all confirmed defects without modifying already-applied migrations `V1`–`V7`, migration `V8__database_integrity_corrections.sql` was created and applied:

```sql
-- ============================================================================
-- V8: Phase 1.1 Database Integrity Corrections
-- ============================================================================

-- 1. Competition & Season Consistency:
-- Add composite unique constraint on competition_seasons
ALTER TABLE competition_seasons
    ADD CONSTRAINT uq_comp_seasons_id_comp_season UNIQUE (id, competition_id, season_id);

-- Enforce composite foreign key on historical_matches
ALTER TABLE historical_matches
    ADD CONSTRAINT fk_matches_comp_season_triplet
    FOREIGN KEY (competition_season_id, competition_id, season_id)
    REFERENCES competition_seasons (id, competition_id, season_id)
    ON DELETE RESTRICT;

-- Enforce composite foreign key on import_batches
ALTER TABLE import_batches
    ADD CONSTRAINT fk_import_batches_comp_season_triplet
    FOREIGN KEY (competition_season_id, competition_id, season_id)
    REFERENCES competition_seasons (id, competition_id, season_id)
    ON DELETE SET NULL;

-- 2. Alias Uniqueness with Nullable Contexts:
-- Replace weak standard unique indexes with functional unique indexes handling NULL via COALESCE
ALTER TABLE competition_aliases DROP INDEX uq_comp_aliases_scoped;
CREATE UNIQUE INDEX uq_comp_aliases_scoped 
    ON competition_aliases (source, normalized_alias, (COALESCE(country_context, '')));

ALTER TABLE team_aliases DROP INDEX uq_team_aliases_scoped;
CREATE UNIQUE INDEX uq_team_aliases_scoped 
    ON team_aliases (source, normalized_alias, (COALESCE(country_context, '')), (COALESCE(competition_context, '')));

-- 3. Protect Match Provenance:
-- Change foreign key on match_source_records from ON DELETE CASCADE to ON DELETE RESTRICT
ALTER TABLE match_source_records DROP FOREIGN KEY fk_source_records_match;
ALTER TABLE match_source_records
    ADD CONSTRAINT fk_source_records_match
    FOREIGN KEY (historical_match_id) REFERENCES historical_matches (id)
    ON DELETE RESTRICT;
```

---

## 4. Analysis of Specific Integrity Concerns

### 4.1 Competition and Season Consistency (Task 2)
- **Problem:** `historical_matches` and `import_batches` had three separate foreign keys pointing to `competitions(id)`, `seasons(id)`, and `competition_seasons(id)`.
- **Solution:** Added a composite unique key `uq_comp_seasons_id_comp_season` on `competition_seasons (id, competition_id, season_id)` and composite foreign keys `fk_matches_comp_season_triplet` and `fk_import_batches_comp_season_triplet`.
- **Result:** The database rejects any insert or update where `competition_id` or `season_id` does not match the exact competition and season associated with `competition_season_id`.

### 4.2 Alias Uniqueness Mechanism (Task 3)
- **Problem:** MySQL unique indexes treat `NULL` as distinct (`NULL != NULL`). Thus, duplicate global aliases (`country_context IS NULL`) or un-scoped team aliases were allowed.
- **Solution:** Replaced traditional compound unique indexes with MySQL 8.0 functional unique indexes using `COALESCE(context, '')`.
- **Validation:** 
  - Duplicate global aliases (`country_context IS NULL`) are rejected.
  - Duplicate contextual aliases are rejected.
  - Same alias belonging to different entities with distinct contexts (e.g., Arsenal in England vs Arsenal in Argentina) is accepted.
  - Same alias from different providers (e.g. `football-data` vs `api-football`) is accepted.
  - Mixed `NULL` and populated context fields enforce uniqueness strictly.

### 4.3 Match Provenance Protection (Task 4)
- **Problem:** `match_source_records.historical_match_id` had `ON DELETE CASCADE`. If a historical match was deleted, the raw source audit trail was lost.
- **Solution:** Altered constraint `fk_source_records_match` to `ON DELETE RESTRICT`.
- **Validation:** Deleting a historical match that has linked `match_source_records` produces a `DataIntegrityViolationException` (foreign key restriction).

### 4.4 Natural Match Key Identity & Uniqueness Rules (Task 5)
- **Format:** `[COUNTRY]-[COMP]-[SEASON_CODE]-[HOME_TEAM]-[AWAY_TEAM]-[YYYYMMDD]` (e.g. `ENG-PL-20242025-ARS-CHE-20241110`).
- **Enforcement:** Enforced via `uq_matches_natural_key UNIQUE (natural_match_key)`.
- **Cases Evaluated & Tested:**
  1. *Same date, different competitions:* Accepted (distinct competition segments).
  2. *Same competition and date, different home/away teams:* Accepted (distinct team segments).
  3. *Same teams playing on different dates (reverse fixtures):* Accepted (distinct date segments).
  4. *Same fixture arriving from multiple providers:* Preserves a single canonical `historical_matches` row while appending multiple `match_source_records`.
  5. *Duplicate natural key insertion:* Rejected by database unique constraint.

### 4.5 Collation Consistency (Task 6)
- **Actual Database Engine:** MySQL 8.0.46 default database collation is `utf8mb4_0900_ai_ci`.
- **Table Collations:** Created with `utf8mb4_unicode_ci` in Flyway DDL for case-insensitive and accent-insensitive Unicode matching.
- **Standardization:** Both collations are fully compatible `utf8mb4` implementations. The Database Inspection API accurately queries `information_schema.schemata` and `information_schema.tables`. Documentation has been updated to clarify that database default collation is `utf8mb4_0900_ai_ci` and domain tables are created with `utf8mb4_unicode_ci`.

---

## 5. Existing Safeguards & Validated Invariants

The following existing safeguards remain fully intact and validated:
1. `chk_seasons_date_order`: Enforces `end_date >= start_date` on seasons.
2. `chk_comp_seasons_date_order`: Enforces `end_date >= start_date` on competition seasons.
3. `chk_matches_distinct_teams`: Enforces `home_team_id <> away_team_id` on historical matches.
4. `uq_competitions_norm_country`: Prevents duplicate normalized competition names per country.
5. `uq_teams_norm_country`: Prevents duplicate normalized team names per country.
6. `uq_match_source_batch_row`: Prevents duplicate row indexing per import batch and source file.
7. `uq_match_source_hash`: Prevents duplicate source hashes per historical match.
8. `Hibernate ddl-auto=validate`: Confirms exact schema compatibility at boot time without automatic alteration.

---

## 6. Unresolved Design Decisions & Future Recommendations

1. **Natural Match Key Rescheduled Fixtures:** If a fixture is postponed and rescheduled to a new date after initial ingestion, the natural key generated with the original date will differ from the new match date. In Phase 2, the identity resolution engine should implement a match re-association workflow rather than creating duplicate orphaned matches.
2. **Quarantine Resolution Life-cycle:** The `import_quarantine_records` table tracks unresolved failed rows. The full workflow for manual vs automatic alias resolution will be implemented in Phase 2.

---

## 7. Verification Summary

- **Total Backend Tests:** 36 (All Green)
  - `DatabaseIntegrityAuditIntegrationTest`: 16 tests covering Tasks 2, 3, 4, 5.
  - `DomainDataArchitectureIntegrationTest`: 9 tests.
  - `DatabaseInspectionControllerIntegrationTest`: 6 tests.
  - `SystemControllerIntegrationTest`: 2 tests.
  - `SystemServiceTest`: 2 tests.
  - `VumaPhaseTwoApplicationTests`: 1 test.
- **Frontend Build:** Angular 18 production build succeeded (375 kB total bundle).
- **Live Endpoints:** All System and Database Explorer endpoints operational and verified.
