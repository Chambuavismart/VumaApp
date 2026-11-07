CREATE TABLE historical_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    season_id BIGINT NOT NULL,
    competition_season_id BIGINT NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    match_date DATE NOT NULL,
    kickoff_time TIME NULL,
    match_status VARCHAR(30) NOT NULL DEFAULT 'FINISHED',
    match_round VARCHAR(50) NULL,
    source_match_identifier VARCHAR(100) NULL,
    
    -- Full-time score
    home_goals INT NULL,
    away_goals INT NULL,
    full_time_result VARCHAR(5) NULL,
    
    -- Half-time score
    half_time_home_goals INT NULL,
    half_time_away_goals INT NULL,
    half_time_result VARCHAR(5) NULL,
    
    -- Match statistics (NULL means not recorded, 0 is recorded zero)
    home_shots INT NULL,
    away_shots INT NULL,
    home_shots_on_target INT NULL,
    away_shots_on_target INT NULL,
    home_corners INT NULL,
    away_corners INT NULL,
    home_fouls INT NULL,
    away_fouls INT NULL,
    home_yellow_cards INT NULL,
    away_yellow_cards INT NULL,
    home_red_cards INT NULL,
    away_red_cards INT NULL,
    
    -- Betting odds (DECIMAL, nullable)
    home_win_odds DECIMAL(8,4) NULL,
    draw_odds DECIMAL(8,4) NULL,
    away_win_odds DECIMAL(8,4) NULL,
    over_2_5_odds DECIMAL(8,4) NULL,
    under_2_5_odds DECIMAL(8,4) NULL,
    over_3_5_odds DECIMAL(8,4) NULL,
    under_3_5_odds DECIMAL(8,4) NULL,
    btts_yes_odds DECIMAL(8,4) NULL,
    btts_no_odds DECIMAL(8,4) NULL,
    
    -- Data integrity & natural key
    natural_match_key VARCHAR(150) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_matches_competition FOREIGN KEY (competition_id) REFERENCES competitions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_matches_season FOREIGN KEY (season_id) REFERENCES seasons (id) ON DELETE RESTRICT,
    CONSTRAINT fk_matches_comp_season FOREIGN KEY (competition_season_id) REFERENCES competition_seasons (id) ON DELETE RESTRICT,
    CONSTRAINT fk_matches_home_team FOREIGN KEY (home_team_id) REFERENCES teams (id) ON DELETE RESTRICT,
    CONSTRAINT fk_matches_away_team FOREIGN KEY (away_team_id) REFERENCES teams (id) ON DELETE RESTRICT,
    CONSTRAINT chk_matches_distinct_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT uq_matches_natural_key UNIQUE (natural_match_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
