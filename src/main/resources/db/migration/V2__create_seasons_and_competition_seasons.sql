CREATE TABLE seasons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_code VARCHAR(50) NOT NULL UNIQUE,
    season_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_seasons_date_order CHECK (end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE competition_seasons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    season_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comp_seasons_competition FOREIGN KEY (competition_id) REFERENCES competitions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_comp_seasons_season FOREIGN KEY (season_id) REFERENCES seasons (id) ON DELETE RESTRICT,
    CONSTRAINT uq_comp_seasons_comp_season UNIQUE (competition_id, season_id),
    CONSTRAINT chk_comp_seasons_date_order CHECK (end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
