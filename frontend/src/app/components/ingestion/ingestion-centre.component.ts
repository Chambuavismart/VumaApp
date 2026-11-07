import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor, NgClass, DecimalPipe, DatePipe, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IngestionService } from '../../services/ingestion.service';
import { DashboardService } from '../../services/dashboard.service';
import { HistoricalDataCoverage } from '../../models/dashboard.model';
import {
  CsvValidationReport,
  FootballDataSourceOption,
  ImportBatchSummaryResponse,
  ImportGroupResponse,
  IngestionDashboardStats,
  QuarantineRecordResponse
} from '../../models/ingestion.models';

@Component({
  selector: 'app-ingestion-centre',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, NgClass, DecimalPipe, DatePipe, SlicePipe, FormsModule],
  templateUrl: './ingestion-centre.component.html',
  styleUrls: ['./ingestion-centre.component.scss']
})
export class IngestionCentreComponent implements OnInit {
  activeTab: 'overview' | 'upload' | 'catalog' | 'history' | 'batches' | 'quarantine' | 'coverage' = 'overview';

  private ingestionService = inject(IngestionService);
  private dashboardService = inject(DashboardService);

  stats: IngestionDashboardStats | null = null;
  catalog: FootballDataSourceOption[] = [];
  importGroups: ImportGroupResponse[] = [];
  importBatches: ImportBatchSummaryResponse[] = [];
  quarantineRecords: QuarantineRecordResponse[] = [];
  dataCoverage: HistoricalDataCoverage[] = [];

  // File Upload & Preview State
  selectedFile: File | null = null;
  uploadedSeasonContext: string = '';
  uploadedCompetitionContext: string = '';
  validationReport: CsvValidationReport | null = null;
  isValidating: boolean = false;
  isImporting: boolean = false;
  importResult: ImportGroupResponse | null = null;

  // Catalog Download State
  isDownloading: boolean = false;
  downloadMessage: string = '';

  // Error/Success state
  errorMessage: string = '';
  successMessage: string = '';

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loadDashboardStats();
    this.loadCatalog();
    this.loadHistory();
    this.loadQuarantine();
    this.loadCoverage();
  }

  loadDashboardStats(): void {
    this.ingestionService.getDashboardStats().subscribe({
      next: (res) => (this.stats = res),
      error: (err) => console.error('Failed to load dashboard stats', err)
    });
  }

  loadCatalog(): void {
    this.ingestionService.getCatalog().subscribe({
      next: (res) => (this.catalog = res),
      error: (err) => console.error('Failed to load catalog', err)
    });
  }

  loadHistory(): void {
    this.ingestionService.getImportGroups().subscribe({
      next: (res) => (this.importGroups = res),
      error: (err) => console.error('Failed to load import groups', err)
    });
    this.ingestionService.getBatches().subscribe({
      next: (res) => (this.importBatches = res),
      error: (err) => console.error('Failed to load batches', err)
    });
  }

  loadQuarantine(): void {
    this.ingestionService.getQuarantineRecords().subscribe({
      next: (res) => (this.quarantineRecords = res),
      error: (err) => console.error('Failed to load quarantine records', err)
    });
  }

  loadCoverage(): void {
    this.dashboardService.getOverview().subscribe({
      next: (res) => (this.dataCoverage = res.dataCoverage || []),
      error: (err) => console.error('Failed to load coverage', err)
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.validationReport = null;
      this.importResult = null;
      this.errorMessage = '';
      this.successMessage = '';
      this.validateFile();
    }
  }

  validateFile(): void {
    if (!this.selectedFile) return;
    this.isValidating = true;
    this.errorMessage = '';

    this.ingestionService
      .validateCsv(this.selectedFile, this.uploadedSeasonContext, this.uploadedCompetitionContext)
      .subscribe({
        next: (report) => {
          this.validationReport = report;
          this.isValidating = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to validate CSV file. Please verify CSV encoding and headers.';
          this.isValidating = false;
        }
      });
  }

  startImport(): void {
    if (!this.selectedFile) return;
    this.isImporting = true;
    this.errorMessage = '';

    this.ingestionService
      .uploadAndImport(this.selectedFile, this.uploadedSeasonContext, this.uploadedCompetitionContext)
      .subscribe({
        next: (res) => {
          this.importResult = res;
          this.isImporting = false;
          this.successMessage = `Import completed with status: ${res.status}. Generated ${res.batches.length} season-specific logical batches.`;
          this.loadAll();
        },
        error: (err) => {
          this.errorMessage = 'Import execution failed. Check server logs for details.';
          this.isImporting = false;
        }
      });
  }

  downloadSource(source: FootballDataSourceOption): void {
    this.isDownloading = true;
    this.downloadMessage = `Downloading and processing ${source.competitionName} (${source.seasonLabel})...`;
    this.errorMessage = '';

    this.ingestionService.downloadAndImport(source.downloadUrl, source.competitionCode, source.seasonLabel).subscribe({
      next: (res) => {
        this.isDownloading = false;
        this.downloadMessage = '';
        this.successMessage = `Successfully ingested ${source.competitionName}: ${res.status} (${res.batches.length} season batches).`;
        this.loadAll();
      },
      error: (err) => {
        this.isDownloading = false;
        this.downloadMessage = '';
        this.errorMessage = `Download or processing failed for ${source.competitionName}. Check network connectivity and server logs.`;
      }
    });
  }

  retryQuarantine(record: QuarantineRecordResponse): void {
    this.ingestionService.retryQuarantinedRecord(record.id).subscribe({
      next: () => {
        this.successMessage = `Quarantine record #${record.id} successfully retried and resolved.`;
        this.loadQuarantine();
        this.loadDashboardStats();
      },
      error: (err) => {
        this.errorMessage = `Retry for record #${record.id} failed. Ensure aliases for Home/Away teams are resolved in Identity Management.`;
      }
    });
  }
}
