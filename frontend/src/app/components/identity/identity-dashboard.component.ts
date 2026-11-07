import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IdentityService, IdentityOverview } from '../../services/identity.service';

@Component({
  selector: 'app-identity-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="identity-dashboard">
      <!-- Header -->
      <div class="page-header">
        <div class="header-left">
          <div class="header-badge">PHASE 2 CANONICAL ENGINE</div>
          <h1 class="page-title">Identity Management System</h1>
          <p class="page-subtitle">Deterministic Team, Competition & Season resolution engine with contextual alias mapping</p>
        </div>
        <div class="header-actions">
          <a routerLink="/resolution-console" class="btn btn-primary">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            Open Resolution Console
          </a>
        </div>
      </div>

      <!-- Loading / Error States -->
      <div *ngIf="loading" class="state-container">
        <div class="spinner"></div>
        <p class="text-muted">Loading live database identity metrics...</p>
      </div>

      <div *ngIf="error" class="alert alert-danger">
        <strong>Error:</strong> {{ error }}
      </div>

      <!-- Metric Cards -->
      <div *ngIf="!loading && overview" class="metrics-grid">
        <div class="metric-card">
          <div class="metric-icon bg-rose-subtle text-rose">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalCanonicalTeams | number }}</span>
            <span class="metric-label">Canonical Teams</span>
          </div>
          <a routerLink="/team-registry" class="card-link">Explore Teams &rarr;</a>
        </div>

        <div class="metric-card">
          <div class="metric-icon bg-blue-subtle text-blue">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalCanonicalCompetitions | number }}</span>
            <span class="metric-label">Competitions</span>
          </div>
          <a routerLink="/competition-registry" class="card-link">Explore Competitions &rarr;</a>
        </div>

        <div class="metric-card">
          <div class="metric-icon bg-emerald-subtle text-emerald">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalSeasons | number }}</span>
            <span class="metric-label">Seasons</span>
          </div>
          <a routerLink="/season-registry" class="card-link">Explore Seasons &rarr;</a>
        </div>

        <div class="metric-card">
          <div class="metric-icon bg-cyan-subtle text-cyan">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalCompetitionSeasons | number }}</span>
            <span class="metric-label">Comp-Season Links</span>
          </div>
          <span class="metric-subtext">Composite FK Protected</span>
        </div>

        <div class="metric-card">
          <div class="metric-icon bg-purple-subtle text-purple">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalTeamAliases | number }}</span>
            <span class="metric-label">Team Aliases</span>
          </div>
          <span class="metric-subtext">Provider & Scoped</span>
        </div>

        <div class="metric-card">
          <div class="metric-icon bg-amber-subtle text-amber">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
          </div>
          <div class="metric-data">
            <span class="metric-value">{{ overview.totalCompetitionAliases | number }}</span>
            <span class="metric-label">Comp Aliases</span>
          </div>
          <span class="metric-subtext">Source Code Mapped</span>
        </div>
      </div>

      <!-- Quick Action Navigation Modules -->
      <div class="modules-grid">
        <div class="module-card">
          <div class="module-header">
            <h3>Team Identity Explorer</h3>
            <span class="badge badge-success">ACTIVE</span>
          </div>
          <p class="module-desc">Browse canonical teams, search by normalized name or country, inspect registered aliases, and map new provider identities safely.</p>
          <a routerLink="/team-registry" class="btn btn-secondary w-full">Manage Teams &rarr;</a>
        </div>

        <div class="module-card">
          <div class="module-header">
            <h3>Competition Identity Explorer</h3>
            <span class="badge badge-success">ACTIVE</span>
          </div>
          <p class="module-desc">Inspect canonical leagues, map Football-Data source codes (E0, E1, D1, I1, SP1), and audit alias uniqueness across countries.</p>
          <a routerLink="/competition-registry" class="btn btn-secondary w-full">Manage Competitions &rarr;</a>
        </div>

        <div class="module-card">
          <div class="module-header">
            <h3>Season Boundaries Explorer</h3>
            <span class="badge badge-success">ACTIVE</span>
          </div>
          <p class="module-desc">Inspect competition-specific calendar boundaries, cross-year formats (e.g. 2024/25), and verify composite foreign key integrity.</p>
          <a routerLink="/season-registry" class="btn btn-secondary w-full">Manage Seasons &rarr;</a>
        </div>

        <div class="module-card highlight">
          <div class="module-header">
            <h3>Interactive Testing Console</h3>
            <span class="badge badge-rose">RESOLVER TOOL</span>
          </div>
          <p class="module-desc">Test real-time identity resolution for teams, competitions, and seasons without importing files. Inspect RESOLVED, UNRESOLVED, and AMBIGUOUS results.</p>
          <a routerLink="/resolution-console" class="btn btn-primary w-full">Launch Console &rarr;</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .identity-dashboard {
      padding: 32px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 32px;
      padding-bottom: 24px;
      border-bottom: 1px solid #1e293b;
    }

    .header-badge {
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      color: #e11d48;
      text-transform: uppercase;
      margin-bottom: 6px;
    }

    .page-title {
      font-size: 1.85rem;
      font-weight: 800;
      color: #f8fafc;
      letter-spacing: -0.02em;
      margin: 0 0 6px 0;
    }

    .page-subtitle {
      font-size: 0.95rem;
      color: #94a3b8;
      margin: 0;
    }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 32px;
    }

    .metric-card {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      position: relative;
      transition: all 0.2s ease;
    }

    .metric-card:hover {
      border-color: #374151;
      transform: translateY(-2px);
    }

    .metric-icon {
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .bg-rose-subtle { background: rgba(225, 29, 72, 0.15); }
    .text-rose { color: #f43f5e; }
    .bg-blue-subtle { background: rgba(59, 130, 246, 0.15); }
    .text-blue { color: #60a5fa; }
    .bg-emerald-subtle { background: rgba(16, 185, 129, 0.15); }
    .text-emerald { color: #34d399; }
    .bg-cyan-subtle { background: rgba(6, 182, 212, 0.15); }
    .text-cyan { color: #22d3ee; }
    .bg-purple-subtle { background: rgba(168, 85, 247, 0.15); }
    .text-purple { color: #c084fc; }
    .bg-amber-subtle { background: rgba(245, 158, 11, 0.15); }
    .text-amber { color: #fbbf24; }

    .metric-value {
      font-size: 1.85rem;
      font-weight: 800;
      color: #f8fafc;
      font-family: ui-monospace, monospace;
      line-height: 1;
    }

    .metric-label {
      font-size: 0.85rem;
      font-weight: 600;
      color: #94a3b8;
    }

    .metric-subtext {
      font-size: 0.75rem;
      color: #64748b;
    }

    .card-link {
      font-size: 0.8rem;
      font-weight: 600;
      color: #e11d48;
      text-decoration: none;
      margin-top: auto;
    }

    .card-link:hover {
      text-decoration: underline;
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 24px;
    }

    .module-card {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 12px;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .module-card.highlight {
      border-color: rgba(225, 29, 72, 0.4);
      background: linear-gradient(180deg, #161a24 0%, #0f1520 100%);
    }

    .module-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .module-header h3 {
      font-size: 1.15rem;
      font-weight: 700;
      color: #f1f5f9;
      margin: 0;
    }

    .module-desc {
      font-size: 0.9rem;
      color: #94a3b8;
      line-height: 1.5;
      margin: 0;
      flex-grow: 1;
    }

    .badge {
      font-size: 0.7rem;
      font-weight: 700;
      padding: 4px 8px;
      border-radius: 6px;
    }

    .badge-success {
      background: rgba(16, 185, 129, 0.2);
      color: #34d399;
      border: 1px solid rgba(16, 185, 129, 0.3);
    }

    .badge-rose {
      background: rgba(225, 29, 72, 0.2);
      color: #f43f5e;
      border: 1px solid rgba(225, 29, 72, 0.3);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 10px 18px;
      font-size: 0.875rem;
      font-weight: 600;
      border-radius: 8px;
      text-decoration: none;
      cursor: pointer;
      transition: all 0.2s ease;
      border: none;
    }

    .btn-primary {
      background: #e11d48;
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(225, 29, 72, 0.3);
    }

    .btn-primary:hover {
      background: #be123c;
    }

    .btn-secondary {
      background: #1e293b;
      color: #e2e8f0;
      border: 1px solid #334155;
    }

    .btn-secondary:hover {
      background: #334155;
      color: #ffffff;
    }

    .w-full {
      width: 100%;
    }

    .state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px;
      gap: 16px;
    }

    .spinner {
      width: 36px;
      height: 36px;
      border: 3px solid #1e293b;
      border-top-color: #e11d48;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .alert {
      padding: 16px 20px;
      border-radius: 8px;
      margin-bottom: 24px;
    }

    .alert-danger {
      background: rgba(239, 68, 68, 0.15);
      border: 1px solid rgba(239, 68, 68, 0.3);
      color: #fca5a5;
    }
  `]
})
export class IdentityDashboardComponent implements OnInit {
  overview: IdentityOverview | null = null;
  loading = true;
  error: string | null = null;

  constructor(private identityService: IdentityService) {}

  ngOnInit(): void {
    this.loadOverview();
  }

  loadOverview(): void {
    this.loading = true;
    this.error = null;
    this.identityService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load identity metrics from server. Ensure backend is running.';
        this.loading = false;
      }
    });
  }
}
