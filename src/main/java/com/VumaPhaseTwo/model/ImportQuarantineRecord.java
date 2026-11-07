package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_quarantine_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportQuarantineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quarantine_import_batch"))
    private ImportBatch importBatch;

    @Column(name = "source_file_name", nullable = false, length = 255)
    private String sourceFileName;

    @Column(name = "source_row_number", nullable = false)
    private Integer sourceRowNumber;

    @Column(name = "raw_row_data", nullable = false, columnDefinition = "TEXT")
    private String rawRowData;

    @Column(name = "failure_category", nullable = false, length = 50)
    private String failureCategory;

    @Column(name = "failure_reason", nullable = false, columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "resolution_status", nullable = false, length = 30)
    @Builder.Default
    private String resolutionStatus = "UNRESOLVED";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
