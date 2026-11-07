package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_source_records", uniqueConstraints = {
    @UniqueConstraint(name = "uq_match_source_batch_row", columnNames = {"import_batch_id", "source_file_name", "source_row_number"}),
    @UniqueConstraint(name = "uq_match_source_hash", columnNames = {"historical_match_id", "source_record_hash"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSourceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historical_match_id", nullable = false, foreignKey = @ForeignKey(name = "fk_source_records_match"))
    private HistoricalMatch historicalMatch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_source_records_import_batch"))
    private ImportBatch importBatch;

    @Column(name = "source_name", nullable = false, length = 50)
    private String sourceName;

    @Column(name = "source_file_name", nullable = false, length = 255)
    private String sourceFileName;

    @Column(name = "source_row_number", nullable = false)
    private Integer sourceRowNumber;

    @Column(name = "source_match_identifier", length = 100)
    private String sourceMatchIdentifier;

    @Column(name = "source_record_hash", nullable = false, length = 64)
    private String sourceRecordHash;

    @CreationTimestamp
    @Column(name = "imported_at", nullable = false, updatable = false)
    private LocalDateTime importedAt;
}
