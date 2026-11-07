package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.TeamAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamAliasRepository extends JpaRepository<TeamAlias, Long> {
    List<TeamAlias> findByTeamId(Long teamId);

    List<TeamAlias> findBySourceAndNormalizedAlias(String source, String normalizedAlias);

    List<TeamAlias> findByNormalizedAlias(String normalizedAlias);

    @Query("SELECT ta FROM TeamAlias ta WHERE ta.source = :source AND ta.normalizedAlias = :normalizedAlias AND " +
           "( (:countryContext IS NULL AND ta.countryContext IS NULL) OR ta.countryContext = :countryContext ) AND " +
           "( (:competitionContext IS NULL AND ta.competitionContext IS NULL) OR ta.competitionContext = :competitionContext )")
    Optional<TeamAlias> findScopedAlias(
            @Param("source") String source,
            @Param("normalizedAlias") String normalizedAlias,
            @Param("countryContext") String countryContext,
            @Param("competitionContext") String competitionContext
    );

    @Query("SELECT ta.source, COUNT(ta) FROM TeamAlias ta GROUP BY ta.source")
    List<Object[]> countBySourceGrouped();
}
