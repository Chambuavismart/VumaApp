CREATE TABLE competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    normalized_name VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL,
    competition_type VARCHAR(50) NOT NULL DEFAULT 'LEAGUE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_competitions_norm_country UNIQUE (normalized_name, country)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE competition_aliases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    source VARCHAR(50) NOT NULL,
    source_code VARCHAR(50) NULL,
    raw_alias VARCHAR(150) NOT NULL,
    normalized_alias VARCHAR(150) NOT NULL,
    country_context VARCHAR(100) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comp_aliases_competition FOREIGN KEY (competition_id) REFERENCES competitions (id) ON DELETE RESTRICT,
    CONSTRAINT uq_comp_aliases_scoped UNIQUE (source, normalized_alias, country_context)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
