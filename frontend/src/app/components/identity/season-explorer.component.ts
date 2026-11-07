import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IdentityService, SeasonDetail, CompetitionDetail, CompetitionSeason } from '../../services/identity.service';

@Component({
  selector: 'app-season-explorer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="season-explorer">
      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="header-badge">CANONICAL REGISTRY</div>
          <h1 class="page-title">Season & Competition Boundaries</h1>
          <p class="page-subtitle">Inspect master canonical seasons, competition-specific calendar boundaries, and verify schedule integrity</p>
        </div>
      </div>

      <!-- Layout -->
      <div class="content-layout">
        <!-- Left: Master Seasons Table -->
        <div class="main-panel">
          <div class="panel-header">
            <div class="search-box">
              <svg class="w-4 h-4 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()" placeholder="Search seasons (e.g. 2024/2025)..." class="search-input" />
            </div>
            <button (click)="loadSeasons()" class="btn btn-secondary btn-sm">Refresh</button>
          </div>

          <!-- Loading / Empty -->
          <div *ngIf="loading" class="state-container">
            <div class="spinner"></div>
            <span>Loading seasons...</span>
          </div>

          <div *ngIf="!loading && seasons.length === 0" class="empty-state">
            <p>No canonical seasons found matching search criteria.</p>
          </div>

          <!-- Table -->
          <div *ngIf="!loading && seasons.length > 0" class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Season Code</th>
                  <th>Season Label</th>
                  <th>Default Start</th>
                  <th>Default End</th>
                  <th>Linked Leagues</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let season of seasons" [class.selected]="selectedSeason?.id === season.id" (click)="selectSeason(season)">
                  <td class="font-mono text-muted">#{{ season.id }}</td>
                  <td class="font-bold text-rose font-mono">{{ season.seasonCode }}</td>
                  <td class="text-white">{{ season.seasonName }}</td>
                  <td class="font-mono text-xs">{{ season.defaultStartDate }}</td>
                  <td class="font-mono text-xs">{{ season.defaultEndDate }}</td>
                  <td>
                    <span class="badge badge-cyan">{{ season.competitionSeasons?.length || 0 }} competitions</span>
                  </td>
                  <td>
                    <button class="btn btn-secondary btn-xs" (click)="selectSeason(season); $event.stopPropagation()">View Links</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Right: Competition-Season Link Details -->
        <div class="detail-panel">
          <div *ngIf="selectedSeason" class="card">
            <div class="card-header">
              <div>
                <span class="text-xs text-rose font-mono">SEASON #{{ selectedSeason.id }}</span>
                <h2 class="card-title">{{ selectedSeason.seasonCode }}</h2>
                <span class="text-xs text-muted">{{ selectedSeason.seasonName }}</span>
              </div>
              <div class="date-bounds font-mono text-xs">
                <div>{{ selectedSeason.defaultStartDate }}</div>
                <div class="text-muted text-center">&darr;</div>
                <div>{{ selectedSeason.defaultEndDate }}</div>
              </div>
            </div>

            <!-- Linked Competitions -->
            <div class="links-section">
              <h3 class="section-heading">Competition-Specific Boundaries ({{ selectedSeason.competitionSeasons.length }})</h3>

              <div *ngIf="selectedSeason.competitionSeasons.length === 0" class="empty-links">
                No competition-specific schedules configured for this season yet.
              </div>

              <div *ngIf="selectedSeason.competitionSeasons.length > 0" class="links-list">
                <div *ngFor="let cs of selectedSeason.competitionSeasons" class="link-card">
                  <div class="link-top">
                    <span class="font-bold text-white">{{ cs.competitionName }}</span>
                    <span class="badge badge-success" *ngIf="cs.active">ACTIVE</span>
                  </div>
                  <div class="link-dates font-mono text-xs">
                    <span class="text-muted">Start: <span class="text-emerald">{{ cs.startDate }}</span></span>
                    <span class="text-muted">End: <span class="text-emerald">{{ cs.endDate }}</span></span>
                  </div>
                  <div class="link-meta text-xs text-muted">
                    Composite Ref: <code class="text-cyan">cs_id: {{ cs.id }}, comp_id: {{ cs.competitionId }}</code>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div *ngIf="!selectedSeason" class="card empty-panel-state">
            <svg class="w-12 h-12 text-muted mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <p class="text-muted">Select a season from the list to inspect configured start and end dates across all participating competitions.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .season-explorer {
      padding: 32px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .page-header {
      margin-bottom: 24px;
      padding-bottom: 20px;
      border-bottom: 1px solid #1e293b;
    }

    .header-badge {
      font-size: 0.75rem;
      font-weight: 700;
      color: #e11d48;
      text-transform: uppercase;
      margin-bottom: 4px;
    }

    .page-title {
      font-size: 1.75rem;
      font-weight: 800;
      color: #f8fafc;
      margin: 0 0 4px 0;
    }

    .page-subtitle {
      font-size: 0.9rem;
      color: #94a3b8;
      margin: 0;
    }

    .content-layout {
      display: grid;
      grid-template-columns: 1fr 440px;
      gap: 24px;
    }

    @media (max-width: 1100px) {
      .content-layout {
        grid-template-columns: 1fr;
      }
    }

    .main-panel, .detail-panel {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .panel-header {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      background: #0f1520;
      padding: 16px;
      border-radius: 10px;
      border: 1px solid #1e293b;
    }

    .search-box {
      display: flex;
      align-items: center;
      gap: 10px;
      background: #0a0e14;
      border: 1px solid #1e293b;
      border-radius: 8px;
      padding: 8px 12px;
      flex-grow: 1;
    }

    .search-input {
      background: transparent;
      border: none;
      color: #f1f5f9;
      font-size: 0.875rem;
      width: 100%;
      outline: none;
    }

    .table-container {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 10px;
      overflow: hidden;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.875rem;
    }

    .data-table th {
      background: #111827;
      color: #94a3b8;
      font-weight: 600;
      padding: 12px 16px;
      border-bottom: 1px solid #1e293b;
    }

    .data-table td {
      padding: 12px 16px;
      border-bottom: 1px solid #1e293b;
      color: #cbd5e1;
    }

    .data-table tbody tr {
      cursor: pointer;
      transition: background 0.15s ease;
    }

    .data-table tbody tr:hover {
      background: rgba(225, 29, 72, 0.05);
    }

    .data-table tbody tr.selected {
      background: rgba(225, 29, 72, 0.12);
      border-left: 3px solid #e11d48;
    }

    .badge-cyan {
      background: rgba(6, 182, 212, 0.15);
      color: #22d3ee;
      border: 1px solid rgba(6, 182, 212, 0.3);
      font-size: 0.75rem;
      padding: 3px 8px;
      border-radius: 6px;
      font-weight: 600;
    }

    .card {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 12px;
      padding: 24px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #1e293b;
    }

    .card-title {
      font-size: 1.35rem;
      font-weight: 800;
      color: #f8fafc;
      margin: 4px 0 0 0;
    }

    .date-bounds {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 6px;
      padding: 6px 10px;
      color: #38bdf8;
    }

    .section-heading {
      font-size: 0.85rem;
      font-weight: 700;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin: 0 0 12px 0;
    }

    .links-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .link-card {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 8px;
      padding: 12px 14px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .link-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .link-dates {
      display: flex;
      gap: 16px;
    }

    .empty-panel-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 48px 24px;
    }

    .empty-links {
      color: #64748b;
      font-size: 0.85rem;
      font-style: italic;
      padding: 12px 0;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 8px 16px;
      font-size: 0.85rem;
      font-weight: 600;
      border-radius: 6px;
      border: none;
      cursor: pointer;
    }

    .btn-sm { padding: 6px 12px; font-size: 0.8rem; }
    .btn-xs { padding: 4px 8px; font-size: 0.75rem; }
    .btn-secondary { background: #1e293b; color: #e2e8f0; border: 1px solid #334155; }
    .btn-secondary:hover { background: #334155; }

    .badge-success { background: rgba(16, 185, 129, 0.2); color: #34d399; }

    .state-container, .empty-state {
      padding: 32px;
      text-align: center;
      color: #94a3b8;
    }

    .spinner {
      width: 28px;
      height: 28px;
      border: 3px solid #1e293b;
      border-top-color: #e11d48;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 12px auto;
    }

    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class SeasonExplorerComponent implements OnInit {
  seasons: SeasonDetail[] = [];
  selectedSeason: SeasonDetail | null = null;
  searchQuery = '';
  loading = true;

  constructor(private identityService: IdentityService) {}

  ngOnInit(): void {
    this.loadSeasons();
  }

  loadSeasons(): void {
    this.loading = true;
    this.identityService.listSeasons(this.searchQuery, 0, 50).subscribe({
      next: (res) => {
        this.seasons = res.content;
        this.loading = false;
        if (this.selectedSeason) {
          const match = this.seasons.find(s => s.id === this.selectedSeason?.id);
          if (match) this.selectedSeason = match;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.loadSeasons();
  }

  selectSeason(season: SeasonDetail): void {
    this.selectedSeason = season;
  }
}
