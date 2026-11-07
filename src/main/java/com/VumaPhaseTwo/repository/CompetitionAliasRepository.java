package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.CompetitionAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionAliasRepository extends JpaRepository<CompetitionAlias, Long> {
    List<CompetitionAlias> findByCompetitionId(Long competitionId);

    List<CompetitionAlias> findBySourceAndNormalizedAlias(String source, String normalizedAlias);

    List<CompetitionAlias> findBySourceAndSourceCode(String source, String sourceCode);

    List<CompetitionAlias> findByNormalizedAlias(String normalizedAlias);

    @Query("SELECT ca FROM CompetitionAlias ca WHERE ca.source = :source AND ca.normalizedAlias = :normalizedAlias AND " +
           "( (:countryContext IS NULL AND ca.countryContext IS NULL) OR ca.countryContext = :countryContext )")
    Optional<CompetitionAlias> findScopedAlias(
            @Param("source") String source,
            @Param("normalizedAlias") String normalizedAlias,
            @Param("countryContext") String countryContext
    );

    @Query("SELECT ca.source, COUNT(ca) FROM CompetitionAlias ca GROUP BY ca.source")
    List<Object[]> countBySourceGrouped();
}
