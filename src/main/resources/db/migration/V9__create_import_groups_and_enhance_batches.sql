-- V9: Phase 3 Import Group and Enhanced Import Batch Tracking
-- Supports 1-to-many relationship between a physical source file (e.g., stacked multi-season CSV) and logical season-specific import batches

CREATE TABLE IF NOT EXISTS import_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_name VARCHAR(50) NOT NULL,
    source_file_name VARCHAR(255) NOT NULL,
    source_file_checksum VARCHAR(64) NOT NULL,
    source_url VARCHAR(500) NULL,
    distribution_format VARCHAR(50) NOT NULL DEFAULT 'SEASON_SPECIFIC',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_physical_rows INT NOT NULL DEFAULT 0,
    total_detected_seasons INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_import_groups_checksum (source_file_checksum),
    INDEX idx_import_groups_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE import_batches
    ADD COLUMN import_group_id BIGINT NULL AFTER id,
    ADD COLUMN distribution_format VARCHAR(50) NOT NULL DEFAULT 'SEASON_SPECIFIC' AFTER import_type,
    ADD COLUMN source_url VARCHAR(500) NULL AFTER source_file_checksum,
    ADD CONSTRAINT fk_import_batches_import_group FOREIGN KEY (import_group_id) REFERENCES import_groups(id) ON DELETE SET NULL;

CREATE INDEX idx_import_batches_group ON import_batches(import_group_id);
