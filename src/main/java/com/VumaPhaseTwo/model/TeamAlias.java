package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_aliases", uniqueConstraints = {
    @UniqueConstraint(name = "uq_team_aliases_scoped", columnNames = {"source", "normalized_alias", "country_context", "competition_context"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_aliases_team"))
    private Team team;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "raw_alias", nullable = false, length = 150)
    private String rawAlias;

    @Column(name = "normalized_alias", nullable = false, length = 150)
    private String normalizedAlias;

    @Column(name = "country_context", length = 100)
    private String countryContext;

    @Column(name = "competition_context", length = 100)
    private String competitionContext;

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
