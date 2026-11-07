package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_group_id", foreignKey = @ForeignKey(name = "fk_import_batches_import_group"))
    private ImportGroup importGroup;

    @Column(name = "source_name", nullable = false, length = 50)
    private String sourceName;

    @Column(name = "source_file_name", nullable = false, length = 255)
    private String sourceFileName;

    @Column(name = "source_file_checksum", nullable = false, length = 64)
    private String sourceFileChecksum;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", foreignKey = @ForeignKey(name = "fk_import_batches_competition"))
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", foreignKey = @ForeignKey(name = "fk_import_batches_season"))
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_season_id", foreignKey = @ForeignKey(name = "fk_import_batches_comp_season"))
    private CompetitionSeason competitionSeason;

    @Column(name = "import_type", nullable = false, length = 50)
    @Builder.Default
    private String importType = "HISTORICAL_CSV";

    @Column(name = "distribution_format", nullable = false, length = 50)
    @Builder.Default
    private String distributionFormat = "SEASON_SPECIFIC";

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "total_rows", nullable = false)
    @Builder.Default
    private Integer totalRows = 0;

    @Column(name = "successful_rows", nullable = false)
    @Builder.Default
    private Integer successfulRows = 0;

    @Column(name = "inserted_rows", nullable = false)
    @Builder.Default
    private Integer insertedRows = 0;

    @Column(name = "updated_rows", nullable = false)
    @Builder.Default
    private Integer updatedRows = 0;

    @Column(name = "skipped_rows", nullable = false)
    @Builder.Default
    private Integer skippedRows = 0;

    @Column(name = "quarantined_rows", nullable = false)
    @Builder.Default
    private Integer quarantinedRows = 0;

    @Column(name = "failed_rows", nullable = false)
    @Builder.Default
    private Integer failedRows = 0;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_summary", columnDefinition = "TEXT")
    private String errorSummary;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
