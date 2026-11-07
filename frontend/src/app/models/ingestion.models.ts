export interface FootballDataSourceOption {
  competitionCode: string;
  competitionName: string;
  country: string;
  distributionFormat: 'SEASON_SPECIFIC' | 'STACKED_MULTI_SEASON';
  seasonLabel: string;
  seasonCode: string;
  downloadUrl: string;
  verified: boolean;
  notes: string;
}

export interface SeasonEligibilitySummary {
  suppliedSeasonLabel: string;
  canonicalSeasonId?: number;
  canonicalSeasonCode?: string;
  canonicalSeasonName?: string;
  canonicalCompetitionId?: number;
  canonicalCompetitionName?: string;
  competitionSeasonId?: number;
  detectedRowCount: number;
  availableRequiredFields: string[];
  availableOptionalFields: string[];
  missingRequiredFields: string[];
  eligible: boolean;
  eligibilityReason: string;
  validationErrors: string[];
}

export interface CsvValidationReport {
  fileName: string;
  fileChecksum: string;
  fileSize: number;
  distributionFormat: 'SEASON_SPECIFIC' | 'STACKED_MULTI_SEASON' | 'UNKNOWN_FORMAT';
  classificationReason: string;
  valid: boolean;
  detectedHeaders: string[];
  totalRows: number;
  hasSeasonColumn: boolean;
  seasonSummaries: SeasonEligibilitySummary[];
  errors: string[];
  warnings: string[];
}

export interface ImportBatchSummaryResponse {
  id: number;
  importGroupId?: number;
  sourceName: string;
  sourceFileName: string;
  sourceFileChecksum: string;
  distributionFormat: string;
  competitionId?: number;
  competitionName?: string;
  seasonId?: number;
  seasonName?: string;
  seasonCode?: string;
  competitionSeasonId?: number;
  status: string;
  totalRows: number;
  successfulRows: number;
  insertedRows: number;
  updatedRows: number;
  skippedRows: number;
  quarantinedRows: number;
  failedRows: number;
  startedAt: string;
  completedAt?: string;
  errorSummary?: string;
}

export interface ImportGroupResponse {
  id: number;
  sourceName: string;
  sourceFileName: string;
  sourceFileChecksum: string;
  sourceUrl?: string;
  distributionFormat: string;
  status: string;
  totalPhysicalRows: number;
  totalDetectedSeasons: number;
  createdAt: string;
  updatedAt: string;
  batches: ImportBatchSummaryResponse[];
}

export interface QuarantineRecordResponse {
  id: number;
  importBatchId?: number;
  sourceFileName: string;
  sourceRowNumber: number;
  failureCategory: string;
  failureReason: string;
  rawData: string;
  resolutionStatus: string;
  resolvedAt?: string;
  notes?: string;
  createdAt: string;
}

export interface IngestionDashboardStats {
  totalPhysicalFilesReceived: number;
  seasonSpecificFilesCount: number;
  stackedFilesCount: number;
  totalImportGroups: number;
  totalSeasonBatches: number;
  completedBatches: number;
  failedBatches: number;
  partialSuccessBatches: number;
  pendingOrProcessingBatches: number;
  totalImportedMatches: number;
  totalDuplicateMatches: number;
  totalQuarantinedRecords: number;
  totalFailedRecords: number;
}
