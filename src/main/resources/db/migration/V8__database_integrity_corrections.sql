-- ============================================================================
-- V8: Phase 1.1 Database Integrity Corrections
-- 1. Enforce Competition & Season Consistency via Composite Foreign Keys
-- 2. Enforce Robust Alias Uniqueness for Nullable Contexts
-- 3. Protect Match Provenance by Restricting Match Deletions
-- ============================================================================

-- 1. Competition & Season Consistency:
-- Add composite unique index on competition_seasons to support composite foreign key references
ALTER TABLE competition_seasons
    ADD CONSTRAINT uq_comp_seasons_id_comp_season UNIQUE (id, competition_id, season_id);

-- Enforce triplet consistency on historical_matches
ALTER TABLE historical_matches
    ADD CONSTRAINT fk_matches_comp_season_triplet
    FOREIGN KEY (competition_season_id, competition_id, season_id)
    REFERENCES competition_seasons (id, competition_id, season_id)
    ON DELETE RESTRICT;

-- Enforce triplet consistency on import_batches
ALTER TABLE import_batches
    ADD CONSTRAINT fk_import_batches_comp_season_triplet
    FOREIGN KEY (competition_season_id, competition_id, season_id)
    REFERENCES competition_seasons (id, competition_id, season_id)
    ON DELETE SET NULL;

-- 2. Alias Uniqueness with Nullable Contexts:
-- Replace weak standard unique indexes with functional unique indexes handling NULL via COALESCE
ALTER TABLE competition_aliases DROP INDEX uq_comp_aliases_scoped;
CREATE UNIQUE INDEX uq_comp_aliases_scoped ON competition_aliases (source, normalized_alias, (COALESCE(country_context, '')));

ALTER TABLE team_aliases DROP INDEX uq_team_aliases_scoped;
CREATE UNIQUE INDEX uq_team_aliases_scoped ON team_aliases (source, normalized_alias, (COALESCE(country_context, '')), (COALESCE(competition_context, '')));

-- 3. Protect Match Provenance:
-- Change foreign key on match_source_records from ON DELETE CASCADE to ON DELETE RESTRICT
ALTER TABLE match_source_records DROP FOREIGN KEY fk_source_records_match;
ALTER TABLE match_source_records
    ADD CONSTRAINT fk_source_records_match
    FOREIGN KEY (historical_match_id) REFERENCES historical_matches (id)
    ON DELETE RESTRICT;
