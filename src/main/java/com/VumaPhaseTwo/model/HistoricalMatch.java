package com.VumaPhaseTwo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "historical_matches", uniqueConstraints = {
    @UniqueConstraint(name = "uq_matches_natural_key", columnNames = {"natural_match_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_competition"))
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_season"))
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_season_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_comp_season"))
    private CompetitionSeason competitionSeason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_home_team"))
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_matches_away_team"))
    private Team awayTeam;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "kickoff_time")
    private LocalTime kickoffTime;

    @Column(name = "match_status", nullable = false, length = 30)
    @Builder.Default
    private String matchStatus = "FINISHED";

    @Column(name = "match_round", length = 50)
    private String matchRound;

    @Column(name = "source_match_identifier", length = 100)
    private String sourceMatchIdentifier;

    // Full-time score
    @Column(name = "home_goals")
    private Integer homeGoals;

    @Column(name = "away_goals")
    private Integer awayGoals;

    @Column(name = "full_time_result", length = 5)
    private String fullTimeResult;

    // Half-time score
    @Column(name = "half_time_home_goals")
    private Integer halfTimeHomeGoals;

    @Column(name = "half_time_away_goals")
    private Integer halfTimeAwayGoals;

    @Column(name = "half_time_result", length = 5)
    private String halfTimeResult;

    // Match statistics (NULL means not recorded, 0 is recorded zero)
    @Column(name = "home_shots")
    private Integer homeShots;

    @Column(name = "away_shots")
    private Integer awayShots;

    @Column(name = "home_shots_on_target")
    private Integer homeShotsOnTarget;

    @Column(name = "away_shots_on_target")
    private Integer awayShotsOnTarget;

    @Column(name = "home_corners")
    private Integer homeCorners;

    @Column(name = "away_corners")
    private Integer awayCorners;

    @Column(name = "home_fouls")
    private Integer homeFouls;

    @Column(name = "away_fouls")
    private Integer awayFouls;

    @Column(name = "home_yellow_cards")
    private Integer homeYellowCards;

    @Column(name = "away_yellow_cards")
    private Integer awayYellowCards;

    @Column(name = "home_red_cards")
    private Integer homeRedCards;

    @Column(name = "away_red_cards")
    private Integer awayRedCards;

    // Betting odds (DECIMAL, nullable)
    @Column(name = "home_win_odds", precision = 8, scale = 4)
    private BigDecimal homeWinOdds;

    @Column(name = "draw_odds", precision = 8, scale = 4)
    private BigDecimal drawOdds;

    @Column(name = "away_win_odds", precision = 8, scale = 4)
    private BigDecimal awayWinOdds;

    @Column(name = "over_2_5_odds", precision = 8, scale = 4)
    private BigDecimal over25Odds;

    @Column(name = "under_2_5_odds", precision = 8, scale = 4)
    private BigDecimal under25Odds;

    @Column(name = "over_3_5_odds", precision = 8, scale = 4)
    private BigDecimal over35Odds;

    @Column(name = "under_3_5_odds", precision = 8, scale = 4)
    private BigDecimal under35Odds;

    @Column(name = "btts_yes_odds", precision = 8, scale = 4)
    private BigDecimal bttsYesOdds;

    @Column(name = "btts_no_odds", precision = 8, scale = 4)
    private BigDecimal bttsNoOdds;

    // Data integrity & natural key
    @Column(name = "natural_match_key", length = 150)
    private String naturalMatchKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
