package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, length = 50)
    private String sourceName;

    @Column(name = "source_file_name", nullable = false)
    private String sourceFileName;

    @Column(name = "source_file_checksum", nullable = false, length = 64)
    private String sourceFileChecksum;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "distribution_format", nullable = false, length = 50)
    private String distributionFormat;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "total_physical_rows", nullable = false)
    private Integer totalPhysicalRows;

    @Column(name = "total_detected_seasons", nullable = false)
    private Integer totalDetectedSeasons;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "importGroup", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<ImportBatch> importBatches = new ArrayList<>();
}
