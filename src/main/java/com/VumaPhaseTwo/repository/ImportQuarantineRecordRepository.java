package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.ImportQuarantineRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportQuarantineRecordRepository extends JpaRepository<ImportQuarantineRecord, Long> {
    List<ImportQuarantineRecord> findByImportBatchId(Long importBatchId);
    List<ImportQuarantineRecord> findByResolutionStatus(String resolutionStatus);
    List<ImportQuarantineRecord> findAllByOrderByCreatedAtDesc();
}
