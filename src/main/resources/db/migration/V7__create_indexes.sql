-- Indexes for historical matches queries
CREATE INDEX idx_matches_comp_season_date ON historical_matches (competition_id, season_id, match_date);
CREATE INDEX idx_matches_comp_season_id_date ON historical_matches (competition_season_id, match_date);
CREATE INDEX idx_matches_home_team_date ON historical_matches (home_team_id, match_date);
CREATE INDEX idx_matches_away_team_date ON historical_matches (away_team_id, match_date);
CREATE INDEX idx_matches_date ON historical_matches (match_date);
CREATE INDEX idx_matches_source_id ON historical_matches (source_match_identifier);

-- Indexes for alias resolution
CREATE INDEX idx_team_aliases_lookup ON team_aliases (normalized_alias, source, country_context);
CREATE INDEX idx_comp_aliases_lookup ON competition_aliases (normalized_alias, source, country_context);
CREATE INDEX idx_comp_aliases_code ON competition_aliases (source_code, source);

-- Indexes for import provenance and batches
CREATE INDEX idx_source_records_batch ON match_source_records (import_batch_id);
CREATE INDEX idx_source_records_match ON match_source_records (historical_match_id);
CREATE INDEX idx_source_records_src_id ON match_source_records (source_match_identifier);
CREATE INDEX idx_quarantine_batch ON import_quarantine_records (import_batch_id);
CREATE INDEX idx_import_batches_status ON import_batches (status);
