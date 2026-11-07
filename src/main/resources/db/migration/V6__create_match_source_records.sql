CREATE TABLE match_source_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    historical_match_id BIGINT NOT NULL,
    import_batch_id BIGINT NOT NULL,
    source_name VARCHAR(50) NOT NULL,
    source_file_name VARCHAR(255) NOT NULL,
    source_row_number INT NOT NULL,
    source_match_identifier VARCHAR(100) NULL,
    source_record_hash VARCHAR(64) NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_source_records_match FOREIGN KEY (historical_match_id) REFERENCES historical_matches (id) ON DELETE CASCADE,
    CONSTRAINT fk_source_records_import_batch FOREIGN KEY (import_batch_id) REFERENCES import_batches (id) ON DELETE RESTRICT,
    CONSTRAINT uq_match_source_batch_row UNIQUE (import_batch_id, source_file_name, source_row_number),
    CONSTRAINT uq_match_source_hash UNIQUE (historical_match_id, source_record_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
