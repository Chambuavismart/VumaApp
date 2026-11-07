import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CsvValidationReport,
  FootballDataSourceOption,
  ImportBatchSummaryResponse,
  ImportGroupResponse,
  IngestionDashboardStats,
  QuarantineRecordResponse
} from '../models/ingestion.models';

@Injectable({
  providedIn: 'root'
})
export class IngestionService {
  private baseUrl = 'http://localhost:8080/api/v1/ingestion';

  constructor(private http: HttpClient) {}

  getCatalog(): Observable<FootballDataSourceOption[]> {
    return this.http.get<FootballDataSourceOption[]>(`${this.baseUrl}/catalog`);
  }

  getDashboardStats(): Observable<IngestionDashboardStats> {
    return this.http.get<IngestionDashboardStats>(`${this.baseUrl}/dashboard/stats`);
  }

  validateCsv(file: File, seasonContext?: string, competitionContext?: string): Observable<CsvValidationReport> {
    const formData = new FormData();
    formData.append('file', file);
    if (seasonContext) formData.append('seasonContext', seasonContext);
    if (competitionContext) formData.append('competitionContext', competitionContext);

    return this.http.post<CsvValidationReport>(`${this.baseUrl}/validate`, formData);
  }

  uploadAndImport(file: File, seasonContext?: string, competitionContext?: string): Observable<ImportGroupResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (seasonContext) formData.append('seasonContext', seasonContext);
    if (competitionContext) formData.append('competitionContext', competitionContext);

    return this.http.post<ImportGroupResponse>(`${this.baseUrl}/upload`, formData);
  }

  downloadAndImport(downloadUrl: string, competitionContext?: string, seasonContext?: string): Observable<ImportGroupResponse> {
    let params = new HttpParams().set('downloadUrl', downloadUrl);
    if (competitionContext) params = params.set('competitionContext', competitionContext);
    if (seasonContext) params = params.set('seasonContext', seasonContext);

    return this.http.post<ImportGroupResponse>(`${this.baseUrl}/download-and-import`, null, { params });
  }

  getImportGroups(): Observable<ImportGroupResponse[]> {
    return this.http.get<ImportGroupResponse[]>(`${this.baseUrl}/groups`);
  }

  getGroupDetails(groupId: number): Observable<ImportGroupResponse> {
    return this.http.get<ImportGroupResponse>(`${this.baseUrl}/groups/${groupId}`);
  }

  getBatches(): Observable<ImportBatchSummaryResponse[]> {
    return this.http.get<ImportBatchSummaryResponse[]>(`${this.baseUrl}/batches`);
  }

  getBatchDetails(batchId: number): Observable<ImportBatchSummaryResponse> {
    return this.http.get<ImportBatchSummaryResponse>(`${this.baseUrl}/batches/${batchId}`);
  }

  getQuarantineRecords(batchId?: number, status?: string): Observable<QuarantineRecordResponse[]> {
    let params = new HttpParams();
    if (batchId) params = params.set('batchId', batchId.toString());
    if (status) params = params.set('status', status);

    return this.http.get<QuarantineRecordResponse[]>(`${this.baseUrl}/quarantine`, { params });
  }

  retryQuarantinedRecord(quarantineId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/quarantine/${quarantineId}/retry`, {});
  }
}
