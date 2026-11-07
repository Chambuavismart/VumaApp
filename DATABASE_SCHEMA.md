# VumaPhaseTwo — Database Architecture & Schema Specification

**Target Database:** MySQL 8.0+ (`Vumaapp` on `localhost:3306`)  
**Engine & Collation:** InnoDB, `utf8mb4`, database default `utf8mb4_0900_ai_ci`, domain tables `utf8mb4_unicode_ci`  
**Migration Strategy:** Flyway version-controlled migrations (`classpath:db/migration`)  
**DDL Mode:** `validate` (Hibernate validates Flyway-managed schema at startup)

---

## 1. Normalized Relational Architecture

The database follows a fully normalized relational model designed for high data integrity, complete import provenance, and high-performance querying without string-based denormalization:

```
[competitions] (1) ───< (N) [competition_aliases]
      │ (1)
      ├───< (N) [competition_seasons] >─── (N) [seasons]
      │                    │ (1)
      │                    └───< (N) [historical_matches] >─── (N) [teams] (1) ───< (N) [team_aliases]
      │                                       │ (1)
      │                                       └───< (N) [match_source_records]
      │                                                         │ (N)
[import_batches] (1) ───────────────────────────────────────────┘
      │ (1)
      └───< (N) [import_quarantine_records]
```

---

## 2. Table Specifications

### 1. `competitions`
Stores canonical football competitions with permanent internal identity.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR 150, NOT NULL) — Display name (e.g., 'Premier League')
- `normalized_name` (VARCHAR 150, NOT NULL) — Lowercase sanitized name
- `country` (VARCHAR 100, NOT NULL)
- `competition_type` (VARCHAR 50, NOT NULL DEFAULT 'LEAGUE')
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `uq_competitions_norm_country` UNIQUE (`normalized_name`, `country`)

### 2. `competition_aliases`
Stores alternative competition names, provider codes, and abbreviations.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `competition_id` (BIGINT, FK -> `competitions.id`)
- `source` (VARCHAR 50, NOT NULL) — Provider name (e.g., 'football-data')
- `source_code` (VARCHAR 50, NULL) — Provider code (e.g., 'E0')
- `raw_alias` (VARCHAR 150, NOT NULL) — Raw text encountered in files
- `normalized_alias` (VARCHAR 150, NOT NULL)
- `country_context` (VARCHAR 100, NULL)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `fk_comp_aliases_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`) ON DELETE RESTRICT
- **Constraint (V8):** `uq_comp_aliases_scoped` UNIQUE (`source`, `normalized_alias`, `(COALESCE(country_context, ''))`)

### 3. `seasons`
Stores canonical global season definitions.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `season_code` (VARCHAR 50, NOT NULL, UNIQUE) — e.g. '2025/2026', '2026'
- `season_name` (VARCHAR 100, NOT NULL)
- `start_date` (DATE, NOT NULL)
- `end_date` (DATE, NOT NULL)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `chk_seasons_date_order` CHECK (`end_date` >= `start_date`)

### 4. `competition_seasons`
Associates competitions with specific season calendar boundaries.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `competition_id` (BIGINT, FK -> `competitions.id`)
- `season_id` (BIGINT, FK -> `seasons.id`)
- `start_date` (DATE, NOT NULL)
- `end_date` (DATE, NOT NULL)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `fk_comp_seasons_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`) ON DELETE RESTRICT
- **Constraint:** `fk_comp_seasons_season` FOREIGN KEY (`season_id`) REFERENCES `seasons` (`id`) ON DELETE RESTRICT
- **Constraint:** `uq_comp_seasons_comp_season` UNIQUE (`competition_id`, `season_id`)
- **Constraint (V8):** `uq_comp_seasons_id_comp_season` UNIQUE (`id`, `competition_id`, `season_id`)
- **Constraint:** `chk_comp_seasons_date_order` CHECK (`end_date` >= `start_date`)

### 5. `teams`
Stores canonical club identities with permanent internal identities.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `name` (VARCHAR 150, NOT NULL)
- `normalized_name` (VARCHAR 150, NOT NULL)
- `country` (VARCHAR 100, NOT NULL)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `uq_teams_norm_country` UNIQUE (`normalized_name`, `country`)

### 6. `team_aliases`
Stores contextual provider aliases without dangerous global merges.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `team_id` (BIGINT, FK -> `teams.id`)
- `source` (VARCHAR 50, NOT NULL)
- `raw_alias` (VARCHAR 150, NOT NULL)
- `normalized_alias` (VARCHAR 150, NOT NULL)
- `country_context` (VARCHAR 100, NULL)
- `competition_context` (VARCHAR 100, NULL)
- `active` (BOOLEAN, NOT NULL DEFAULT TRUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `fk_team_aliases_team` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT
- **Constraint (V8):** `uq_team_aliases_scoped` UNIQUE (`source`, `normalized_alias`, `(COALESCE(country_context, ''))`, `(COALESCE(competition_context, ''))`)

### 7. `historical_matches`
Central fixture, result, statistics, and odds record.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `competition_id` (BIGINT, FK -> `competitions.id`)
- `season_id` (BIGINT, FK -> `seasons.id`)
- `competition_season_id` (BIGINT, FK -> `competition_seasons.id`)
- `home_team_id` (BIGINT, FK -> `teams.id`)
- `away_team_id` (BIGINT, FK -> `teams.id`)
- `match_date` (DATE, NOT NULL)
- `kickoff_time` (TIME, NULL)
- `match_status` (VARCHAR 30, NOT NULL DEFAULT 'FINISHED')
- `match_round` (VARCHAR 50, NULL)
- `source_match_identifier` (VARCHAR 100, NULL)
- `home_goals`, `away_goals` (INT, NULL)
- `full_time_result` (VARCHAR 5, NULL) — 'H', 'D', 'A'
- `half_time_home_goals`, `half_time_away_goals` (INT, NULL)
- `half_time_result` (VARCHAR 5, NULL)
- `home_shots`, `away_shots`, `home_shots_on_target`, `away_shots_on_target` (INT, NULL)
- `home_corners`, `away_corners`, `home_fouls`, `away_fouls` (INT, NULL)
- `home_yellow_cards`, `away_yellow_cards`, `home_red_cards`, `away_red_cards` (INT, NULL)
- `home_win_odds`, `draw_odds`, `away_win_odds` (DECIMAL(8,4), NULL)
- `over_2_5_odds`, `under_2_5_odds`, `over_3_5_odds`, `under_3_5_odds`, `btts_yes_odds`, `btts_no_odds` (DECIMAL(8,4), NULL)
- `natural_match_key` (VARCHAR 150, NULL, UNIQUE)
- `created_at`, `updated_at` (TIMESTAMP)
- **Constraint:** `fk_matches_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`) ON DELETE RESTRICT
- **Constraint:** `fk_matches_season` FOREIGN KEY (`season_id`) REFERENCES `seasons` (`id`) ON DELETE RESTRICT
- **Constraint:** `fk_matches_comp_season` FOREIGN KEY (`competition_season_id`) REFERENCES `competition_seasons` (`id`) ON DELETE RESTRICT
- **Constraint (V8):** `fk_matches_comp_season_triplet` FOREIGN KEY (`competition_season_id`, `competition_id`, `season_id`) REFERENCES `competition_seasons` (`id`, `competition_id`, `season_id`) ON DELETE RESTRICT
- **Constraint:** `fk_matches_home_team` FOREIGN KEY (`home_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT
- **Constraint:** `fk_matches_away_team` FOREIGN KEY (`away_team_id`) REFERENCES `teams` (`id`) ON DELETE RESTRICT
- **Constraint:** `chk_matches_distinct_teams` CHECK (`home_team_id` <> `away_team_id`)
- **Constraint:** `uq_matches_natural_key` UNIQUE (`natural_match_key`)

### 8. `import_batches`
Audits every CSV or historical data ingestion execution.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `source_name` (VARCHAR 50, NOT NULL)
- `source_file_name` (VARCHAR 255, NOT NULL)
- `source_file_checksum` (VARCHAR 64, NOT NULL)
- `competition_id`, `season_id`, `competition_season_id` (BIGINT, FK, NULLABLE)
- `import_type` (VARCHAR 50, NOT NULL DEFAULT 'HISTORICAL_CSV')
- `status` (VARCHAR 30, NOT NULL DEFAULT 'PENDING')
- `total_rows`, `successful_rows`, `inserted_rows`, `updated_rows`, `skipped_rows`, `quarantined_rows`, `failed_rows` (INT NOT NULL DEFAULT 0)
- `started_at` (TIMESTAMP NOT NULL)
- `completed_at` (TIMESTAMP NULL)
- `error_summary` (TEXT NULL)
- **Constraint:** `fk_import_batches_competition` FOREIGN KEY (`competition_id`) REFERENCES `competitions` (`id`) ON DELETE SET NULL
- **Constraint:** `fk_import_batches_season` FOREIGN KEY (`season_id`) REFERENCES `seasons` (`id`) ON DELETE SET NULL
- **Constraint:** `fk_import_batches_comp_season` FOREIGN KEY (`competition_season_id`) REFERENCES `competition_seasons` (`id`) ON DELETE SET NULL
- **Constraint (V8):** `fk_import_batches_comp_season_triplet` FOREIGN KEY (`competition_season_id`, `competition_id`, `season_id`) REFERENCES `competition_seasons` (`id`, `competition_id`, `season_id`) ON DELETE SET NULL

### 9. `import_quarantine_records`
Preserves invalid or unresolvable rows for subsequent remediation.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `import_batch_id` (BIGINT, FK -> `import_batches.id`)
- `source_file_name` (VARCHAR 255, NOT NULL)
- `source_row_number` (INT, NOT NULL)
- `raw_row_data` (TEXT, NOT NULL)
- `failure_category` (VARCHAR 50, NOT NULL)
- `failure_reason` (TEXT, NOT NULL)
- `resolution_status` (VARCHAR 30, NOT NULL DEFAULT 'UNRESOLVED')
- `created_at` (TIMESTAMP NOT NULL), `resolved_at` (TIMESTAMP NULL)

### 10. `match_source_records`
Preserves full source lineage and many-to-many relationship between matches and import files/batches.
- `id` (BIGINT, PK, AUTO_INCREMENT)
- `historical_match_id` (BIGINT, FK -> `historical_matches.id` ON DELETE RESTRICT)
- `import_batch_id` (BIGINT, FK -> `import_batches.id` ON DELETE RESTRICT)
- `source_name` (VARCHAR 50, NOT NULL)
- `source_file_name` (VARCHAR 255, NOT NULL)
- `source_row_number` (INT, NOT NULL)
- `source_match_identifier` (VARCHAR 100, NULL)
- `source_record_hash` (VARCHAR 64, NOT NULL)
- `imported_at` (TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)
- **Constraint (V8):** `fk_source_records_match` FOREIGN KEY (`historical_match_id`) REFERENCES `historical_matches` (`id`) ON DELETE RESTRICT
- **Constraint:** `fk_source_records_import_batch` FOREIGN KEY (`import_batch_id`) REFERENCES `import_batches` (`id`) ON DELETE RESTRICT
- **Constraint:** `uq_match_source_batch_row` UNIQUE (`import_batch_id`, `source_file_name`, `source_row_number`)
- **Constraint:** `uq_match_source_hash` UNIQUE (`historical_match_id`, `source_record_hash`)

---

## 3. Index Strategy and Query Readiness

The schema defines dedicated non-redundant indexes for high query performance:

| Index Name | Table | Columns | Purpose |
|------------|-------|---------|---------|
| `idx_matches_comp_season_date` | `historical_matches` | `(competition_id, season_id, match_date)` | Filtering matches by competition and season chronologically |
| `idx_matches_comp_season_id_date` | `historical_matches` | `(competition_season_id, match_date)` | Direct retrieval by competition season boundary |
| `idx_matches_home_team_date` | `historical_matches` | `(home_team_id, match_date)` | Team home form, latest N home matches |
| `idx_matches_away_team_date` | `historical_matches` | `(away_team_id, match_date)` | Team away form, latest N away matches |
| `idx_matches_date` | `historical_matches` | `(match_date)` | Date range filters and as-of-date cutoff queries |
| `idx_matches_source_id` | `historical_matches` | `(source_match_identifier)` | Lookup by external fixture ID |
| `idx_team_aliases_lookup` | `team_aliases` | `(normalized_alias, source, country_context)` | High-speed alias resolution during CSV ingestion |
| `idx_comp_aliases_lookup` | `competition_aliases` | `(normalized_alias, source, country_context)` | Alias resolution for league headers |
| `idx_comp_aliases_code` | `competition_aliases` | `(source_code, source)` | Rapid resolution of source codes like 'E0' |
| `idx_source_records_batch` | `match_source_records` | `(import_batch_id)` | Audit drill-downs per batch |
| `idx_source_records_match` | `match_source_records` | `(historical_match_id)` | Lineage retrieval per match |
| `idx_quarantine_batch` | `import_quarantine_records`| `(import_batch_id)` | Batch quarantine inspection |
| `idx_import_batches_status` | `import_batches` | `(status)` | Batch monitoring dashboard |

---

## 4. Flyway Migration Versioning

- `V1__create_competitions_tables.sql` — Competitions and competition aliases
- `V2__create_seasons_and_competition_seasons.sql` — Seasons and competition seasons
- `V3__create_teams_and_aliases.sql` — Canonical teams and contextual team aliases
- `V4__create_historical_matches.sql` — Central historical matches table
- `V5__create_import_batches_and_quarantine.sql` — Import batches and quarantine records
- `V6__create_match_source_records.sql` — Match source lineage records
- `V7__create_indexes.sql` — Performance and lookup index creation
- `V8__database_integrity_corrections.sql` — Phase 1.1 composite foreign keys, alias uniqueness functional indexes, match provenance protection
