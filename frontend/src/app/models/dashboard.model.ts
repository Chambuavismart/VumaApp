export interface HistoricalDataCoverage {
  competitionId: number;
  competitionName: string;
  country: string;
  seasonId: number;
  seasonCode: string;
  seasonName: string;
  matchCount: number;
  firstMatchDate: string | null;
  lastMatchDate: string | null;
  active: boolean;
}

export interface RecentImportActivity {
  batchId: number;
  importGroupId: number | null;
  sourceName: string;
  sourceFileName: string;
  competitionName: string | null;
  seasonCode: string | null;
  seasonName: string | null;
  status: string;
  totalRows: number;
  importedRows: number;
  quarantinedRows: number;
  failedRows: number;
  distributionFormat: string;
  startedAt: string;
  completedAt: string | null;
}

export interface DashboardOverview {
  totalHistoricalMatches: number;
  totalCanonicalTeams: number;
  totalCompetitions: number;
  totalSeasons: number;
  totalCompetitionSeasons: number;
  totalImportBatches: number;
  totalQuarantinedRecords: number;
  dataCoverage: HistoricalDataCoverage[];
  recentImports: RecentImportActivity[];
}
