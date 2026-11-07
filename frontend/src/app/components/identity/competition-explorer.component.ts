import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IdentityService, CompetitionDetail } from '../../services/identity.service';

@Component({
  selector: 'app-competition-explorer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="competition-explorer">
      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="header-badge">CANONICAL REGISTRY</div>
          <h1 class="page-title">Competition Identity Explorer</h1>
          <p class="page-subtitle">Inspect canonical leagues & tournaments, map Football-Data source codes (E0, E1, D1, I1, SP1), and manage aliases</p>
        </div>
      </div>

      <!-- Main Layout -->
      <div class="content-layout">
        <!-- Left: Competition List & Search -->
        <div class="main-panel">
          <div class="panel-header">
            <div class="search-box">
              <svg class="w-4 h-4 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()" placeholder="Search competitions or country..." class="search-input" />
            </div>
            <button (click)="loadCompetitions()" class="btn btn-secondary btn-sm">Refresh</button>
          </div>

          <!-- Loading / Empty -->
          <div *ngIf="loading" class="state-container">
            <div class="spinner"></div>
            <span>Loading competitions...</span>
          </div>

          <div *ngIf="!loading && competitions.length === 0" class="empty-state">
            <p>No canonical competitions found matching search criteria.</p>
          </div>

          <!-- Table -->
          <div *ngIf="!loading && competitions.length > 0" class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Canonical Name</th>
                  <th>Country</th>
                  <th>Type</th>
                  <th>Source Codes & Aliases</th>
                  <th>Seasons</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let comp of competitions" [class.selected]="selectedComp?.id === comp.id" (click)="selectComp(comp)">
                  <td class="font-mono text-muted">#{{ comp.id }}</td>
                  <td class="font-bold text-white">{{ comp.name }}</td>
                  <td><span class="badge badge-country">{{ comp.country }}</span></td>
                  <td class="text-xs text-muted">{{ comp.competitionType }}</td>
                  <td>
                    <div class="code-pills">
                      <ng-container *ngFor="let a of comp.aliases">
                        <span *ngIf="a.sourceCode" class="pill-code">{{ a.sourceCode }}</span>
                      </ng-container>
                      <span class="alias-count">{{ comp.aliases?.length || 0 }} mapped</span>
                    </div>
                  </td>
                  <td><span class="season-count">{{ comp.seasons?.length || 0 }} configured</span></td>
                  <td>
                    <span class="badge" [class.badge-success]="comp.active" [class.badge-secondary]="!comp.active">
                      {{ comp.active ? 'ACTIVE' : 'INACTIVE' }}
                    </span>
                  </td>
                  <td>
                    <button class="btn btn-secondary btn-xs" (click)="selectComp(comp); $event.stopPropagation()">View & Map</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Right: Detail & Mapping Panel -->
        <div class="detail-panel">
          <div *ngIf="selectedComp" class="card">
            <div class="card-header">
              <div>
                <span class="text-xs text-rose font-mono">COMPETITION #{{ selectedComp.id }}</span>
                <h2 class="card-title">{{ selectedComp.name }}</h2>
              </div>
              <span class="badge badge-country">{{ selectedComp.country }}</span>
            </div>

            <!-- Aliases & Source Codes -->
            <div class="alias-section">
              <h3 class="section-heading">Registered Source Codes & Aliases ({{ selectedComp.aliases.length }})</h3>
              
              <div *ngIf="selectedComp.aliases.length === 0" class="empty-aliases">
                No provider codes or aliases mapped to this competition yet.
              </div>

              <div *ngIf="selectedComp.aliases.length > 0" class="alias-list">
                <div *ngFor="let alias of selectedComp.aliases" class="alias-card">
                  <div class="alias-top">
                    <span class="alias-source">{{ alias.source }}</span>
                    <span *ngIf="alias.sourceCode" class="pill-code">{{ alias.sourceCode }}</span>
                    <span class="alias-raw">{{ alias.rawAlias }}</span>
                  </div>
                  <div class="alias-meta">
                    <span class="text-xs text-muted">Normalized: <code class="text-rose">{{ alias.normalizedAlias }}</code></span>
                    <span *ngIf="alias.countryContext" class="text-xs text-muted">Country Context: {{ alias.countryContext }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Register New Competition Alias -->
            <div class="register-section">
              <h3 class="section-heading">Map New Provider Code / Alias</h3>
              <form (ngSubmit)="registerAlias()" class="alias-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>Provider / Source <span class="text-rose">*</span></label>
                    <input type="text" [(ngModel)]="newAliasSource" name="source" placeholder="e.g. football-data" required class="form-input" />
                  </div>
                  <div class="form-group">
                    <label>Source Code (e.g. E0, E1)</label>
                    <input type="text" [(ngModel)]="newAliasSourceCode" name="sourceCode" placeholder="e.g. E0" class="form-input" />
                  </div>
                </div>
                <div class="form-group">
                  <label>Raw Incoming Name / Code <span class="text-rose">*</span></label>
                  <input type="text" [(ngModel)]="newAliasRaw" name="rawAlias" placeholder="e.g. English Premier League, E0" required class="form-input" />
                </div>
                <div class="form-group">
                  <label>Country Context (optional)</label>
                  <input type="text" [(ngModel)]="newAliasCountry" name="countryContext" placeholder="e.g. England" class="form-input" />
                </div>

                <div *ngIf="registerMessage" class="alert" [class.alert-success]="registerSuccess" [class.alert-danger]="!registerSuccess">
                  {{ registerMessage }}
                </div>

                <button type="submit" [disabled]="registering" class="btn btn-primary w-full">
                  {{ registering ? 'Registering...' : 'Register Competition Alias' }}
                </button>
              </form>
            </div>
          </div>

          <div *ngIf="!selectedComp" class="card empty-panel-state">
            <svg class="w-12 h-12 text-muted mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            <p class="text-muted">Select a competition from the table to view source code mappings and register new aliases.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .competition-explorer {
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

    .badge-country {
      background: #1e293b;
      color: #38bdf8;
      border: 1px solid #334155;
      font-size: 0.75rem;
      padding: 3px 8px;
      border-radius: 6px;
      font-weight: 600;
    }

    .code-pills {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
    }

    .pill-code {
      background: rgba(225, 29, 72, 0.2);
      color: #f43f5e;
      border: 1px solid rgba(225, 29, 72, 0.35);
      font-family: ui-monospace, monospace;
      font-weight: 700;
      font-size: 0.75rem;
      padding: 1px 6px;
      border-radius: 4px;
    }

    .alias-count, .season-count {
      font-size: 0.75rem;
      color: #94a3b8;
      background: #1e293b;
      padding: 2px 6px;
      border-radius: 4px;
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

    .section-heading {
      font-size: 0.85rem;
      font-weight: 700;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin: 0 0 12px 0;
    }

    .alias-section {
      margin-bottom: 24px;
      padding-bottom: 20px;
      border-bottom: 1px solid #1e293b;
    }

    .alias-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-height: 200px;
      overflow-y: auto;
    }

    .alias-card {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 8px;
      padding: 10px 14px;
    }

    .alias-top {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
    }

    .alias-source {
      font-size: 0.75rem;
      font-weight: 700;
      color: #38bdf8;
      text-transform: uppercase;
    }

    .alias-raw {
      font-weight: 700;
      color: #f8fafc;
      font-size: 0.875rem;
    }

    .alias-meta {
      display: flex;
      gap: 12px;
    }

    .alias-form {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .form-group label {
      font-size: 0.8rem;
      font-weight: 600;
      color: #94a3b8;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }

    .form-input {
      background: #0a0e14;
      border: 1px solid #1e293b;
      border-radius: 8px;
      padding: 8px 12px;
      color: #f1f5f9;
      font-size: 0.875rem;
      outline: none;
    }

    .form-input:focus {
      border-color: #e11d48;
    }

    .empty-panel-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 48px 24px;
    }

    .empty-aliases {
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
    .btn-primary { background: #e11d48; color: #fff; }
    .btn-primary:hover { background: #be123c; }
    .btn-secondary { background: #1e293b; color: #e2e8f0; border: 1px solid #334155; }
    .btn-secondary:hover { background: #334155; }

    .badge-success { background: rgba(16, 185, 129, 0.2); color: #34d399; }
    .badge-secondary { background: rgba(148, 163, 184, 0.2); color: #94a3b8; }

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

    .alert { padding: 10px 14px; border-radius: 6px; font-size: 0.85rem; }
    .alert-success { background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #34d399; }
    .alert-danger { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #fca5a5; }
    .w-full { width: 100%; }
  `]
})
export class CompetitionExplorerComponent implements OnInit {
  competitions: CompetitionDetail[] = [];
  selectedComp: CompetitionDetail | null = null;
  searchQuery = '';
  loading = true;

  // New alias form
  newAliasSource = '';
  newAliasSourceCode = '';
  newAliasRaw = '';
  newAliasCountry = '';
  registering = false;
  registerMessage: string | null = null;
  registerSuccess = false;

  constructor(private identityService: IdentityService) {}

  ngOnInit(): void {
    this.loadCompetitions();
  }

  loadCompetitions(): void {
    this.loading = true;
    this.identityService.listCompetitions(this.searchQuery, 0, 50).subscribe({
      next: (res) => {
        this.competitions = res.content;
        this.loading = false;
        if (this.selectedComp) {
          const match = this.competitions.find(c => c.id === this.selectedComp?.id);
          if (match) this.selectedComp = match;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.loadCompetitions();
  }

  selectComp(comp: CompetitionDetail): void {
    this.selectedComp = comp;
    this.registerMessage = null;
  }

  registerAlias(): void {
    if (!this.selectedComp || !this.newAliasSource || !this.newAliasRaw) return;

    this.registering = true;
    this.registerMessage = null;

    this.identityService.registerCompetitionAlias(this.selectedComp.id, {
      source: this.newAliasSource,
      sourceCode: this.newAliasSourceCode || undefined,
      rawAlias: this.newAliasRaw,
      countryContext: this.newAliasCountry || undefined
    }).subscribe({
      next: (created) => {
        this.registering = false;
        this.registerSuccess = true;
        this.registerMessage = `Successfully mapped alias '${created.rawAlias}' to ${this.selectedComp?.name}`;
        this.newAliasRaw = '';
        this.newAliasSourceCode = '';
        this.loadCompetitions();
      },
      error: (err) => {
        this.registering = false;
        this.registerSuccess = false;
        this.registerMessage = err.error || 'Failed to register alias. Check for uniqueness conflicts.';
      }
    });
  }
}
