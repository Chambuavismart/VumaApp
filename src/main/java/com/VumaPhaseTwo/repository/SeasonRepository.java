package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findBySeasonCode(String seasonCode);

    List<Season> findBySeasonCodeContainingIgnoreCaseOrSeasonNameContainingIgnoreCase(String seasonCode, String seasonName);

    @Query("SELECT s FROM Season s WHERE LOWER(s.seasonCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.seasonName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Season> searchSeasons(@Param("query") String query, Pageable pageable);
}
