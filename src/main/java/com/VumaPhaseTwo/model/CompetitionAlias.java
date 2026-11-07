package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition_aliases", uniqueConstraints = {
    @UniqueConstraint(name = "uq_comp_aliases_scoped", columnNames = {"source", "normalized_alias", "country_context"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comp_aliases_competition"))
    private Competition competition;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "source_code", length = 50)
    private String sourceCode;

    @Column(name = "raw_alias", nullable = false, length = 150)
    private String rawAlias;

    @Column(name = "normalized_alias", nullable = false, length = 150)
    private String normalizedAlias;

    @Column(name = "country_context", length = 100)
    private String countryContext;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
