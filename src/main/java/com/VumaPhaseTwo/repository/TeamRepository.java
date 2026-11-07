package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByNormalizedNameAndCountry(String normalizedName, String country);

    List<Team> findByNormalizedName(String normalizedName);

    Page<Team> findByNameContainingIgnoreCaseOrNormalizedNameContainingIgnoreCase(String name, String normalizedName, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.country) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Team> searchTeams(@Param("query") String query, Pageable pageable);
}
