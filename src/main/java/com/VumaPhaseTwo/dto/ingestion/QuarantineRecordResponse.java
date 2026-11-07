package com.VumaPhaseTwo.dto.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarantineRecordResponse {
    private Long id;
    private Long importBatchId;
    private String sourceFileName;
    private Integer sourceRowNumber;
    private String failureCategory;
    private String failureReason;
    private String rawData;
    private String resolutionStatus;
    private String resolvedAt;
    private String notes;
    private String createdAt;
}
