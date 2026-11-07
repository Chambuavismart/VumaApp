package com.VumaPhaseTwo.repository;

import com.VumaPhaseTwo.model.MatchSourceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchSourceRecordRepository extends JpaRepository<MatchSourceRecord, Long> {
    List<MatchSourceRecord> findByHistoricalMatchId(Long historicalMatchId);
    List<MatchSourceRecord> findByImportBatchId(Long importBatchId);
    Optional<MatchSourceRecord> findByImportBatchIdAndSourceFileNameAndSourceRowNumber(
            Long importBatchId, String sourceFileName, Integer sourceRowNumber);
    Optional<MatchSourceRecord> findByHistoricalMatchIdAndSourceRecordHash(
            Long historicalMatchId, String sourceRecordHash);
    Optional<MatchSourceRecord> findBySourceRecordHash(String sourceRecordHash);
}
