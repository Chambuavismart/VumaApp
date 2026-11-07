package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    Optional<Competition> findByNormalizedNameAndCountry(String normalizedName, String country);

    List<Competition> findByNormalizedName(String normalizedName);

    Page<Competition> findByNameContainingIgnoreCaseOrNormalizedNameContainingIgnoreCase(String name, String normalizedName, Pageable pageable);

    @Query("SELECT c FROM Competition c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.country) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Competition> searchCompetitions(@Param("query") String query, Pageable pageable);
}
