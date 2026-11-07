# VumaPhaseTwo — Historical Data Ingestion & Identity Resolution Architecture

## 1. Principles of Data Ingestion

1. **Idempotency:** Re-importing the same CSV file updates existing records without creating duplicates.
2. **Provenance Preservation:** Every match maintains a persistent link to its `import_batch_id` and source file metadata.
3. **Canonical Identity Resolution:** Team names in raw CSV files resolve through the `team_aliases` registry.
4. **Quarantine Routing:** Unresolved aliases or malformed rows are isolated into `import_quarantine_records` rather than failing the entire batch or guessing identities.
5. **Deterministic Natural Keys:** Fixture uniqueness is enforced by `(competition_id, season_id, match_date, home_team_id, away_team_id)`.
6. **Strict Null Preservation:** Missing statistics or odds remain `NULL` and are never coerced to zero.
