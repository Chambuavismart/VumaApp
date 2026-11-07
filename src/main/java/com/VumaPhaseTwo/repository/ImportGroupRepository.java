package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.ImportGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportGroupRepository extends JpaRepository<ImportGroup, Long> {
    List<ImportGroup> findAllByOrderByCreatedAtDesc();
    Optional<ImportGroup> findBySourceFileChecksum(String checksum);
}
