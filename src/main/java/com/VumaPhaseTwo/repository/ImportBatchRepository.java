package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    List<ImportBatch> findByStatus(String status);
    Optional<ImportBatch> findBySourceFileNameAndSourceFileChecksum(String sourceFileName, String sourceFileChecksum);
    List<ImportBatch> findByImportGroupId(Long importGroupId);
    List<ImportBatch> findAllByOrderByCreatedAtDesc();
    List<ImportBatch> findTop15ByOrderByStartedAtDesc();
}
