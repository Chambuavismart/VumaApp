import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-matches-hub',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="matches-page">
      <div class="page-header">
        <div>
          <div class="header-badge">PHASE 4 ARCHITECTURAL DESTINATION</div>
          <h1 class="page-title">Historical Matches Engine</h1>
          <p class="page-subtitle">Centralized high-performance match retrieval engine and multi-archetype query pipeline</p>
        </div>
      </div>

      <div class="card status-card">
        <div class="status-icon">
          <svg class="w-12 h-12 text-rose" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
        </div>
        <div class="status-content">
          <span class="badge badge-warning">Awaiting Phase 4 Authorization</span>
          <h2>Historical Match Retrieval Engine</h2>
          <p class="description">
            The database currently holds 3NF normalized match records. The historical match querying capability is scheduled as the primary deliverable for Phase 4.
            Per architecture specifications, fake tables and fabricated match rows are strictly prohibited.
          </p>
        </div>
      </div>

      <div class="card mt-4">
        <h3 class="section-title">Planned Phase 4 Deliverables</h3>
        <div class="deliverables-grid">
          <div class="deliverable-item">
            <div class="num-badge">01</div>
            <div>
              <h4>Unified HistoricalMatchQueryService</h4>
              <p>Supporting all 6 query archetypes (latest N fixtures home/away, head-to-head, date range, competition-season, team form).</p>
            </div>
          </div>
          <div class="deliverable-item">
            <div class="num-badge">02</div>
            <div>
              <h4>High-Speed Indexed Union Queries</h4>
              <p>Index-optimized composite queries for team performance analysis without full-table scans.</p>
            </div>
          </div>
          <div class="deliverable-item">
            <div class="num-badge">03</div>
            <div>
              <h4>Symmetric H2H Matrix Calculator</h4>
              <p>Calculating bilateral historical records, goal averages, clean sheets, and margin distributions.</p>
            </div>
          </div>
          <div class="deliverable-item">
            <div class="num-badge">04</div>
            <div>
              <h4>Strict As-Of-Date Cutoff</h4>
              <p>Point-in-time state reconstruction for statistical research without future lookahead bias.</p>
            </div>
          </div>
        </div>

        <div class="actions mt-4">
          <a routerLink="/dashboard" class="btn btn-secondary">Return to Dashboard</a>
          <a routerLink="/data-centre" class="btn btn-primary">Inspect Data Ingestion</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .matches-page {
      padding: 24px 32px;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
    }

    .header-badge {
      display: inline-block;
      padding: 4px 10px;
      background: rgba(225, 29, 72, 0.15);
      border: 1px solid rgba(225, 29, 72, 0.4);
      color: #fb7185;
      border-radius: 4px;
      font-size: 0.7rem;
      font-weight: 700;
      letter-spacing: 0.06em;
      margin-bottom: 8px;
    }

    .page-title {
      font-size: 1.6rem;
      font-weight: 800;
      color: #f8fafc;
      margin: 0 0 6px 0;
      letter-spacing: -0.02em;
    }

    .page-subtitle {
      color: #94a3b8;
      font-size: 0.875rem;
      margin: 0;
    }

    .card {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 8px;
      padding: 24px;
    }

    .status-card {
      display: flex;
      align-items: center;
      gap: 24px;
      margin-top: 24px;
      background: linear-gradient(135deg, #141c28 0%, #0f1520 100%);

      h2 {
        font-size: 1.25rem;
        font-weight: 800;
        color: #f8fafc;
        margin: 8px 0;
      }

      .description {
        color: #94a3b8;
        font-size: 0.875rem;
        margin: 0;
        line-height: 1.5;
        max-width: 750px;
      }
    }

    .section-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: #f8fafc;
      margin: 0 0 16px 0;
    }

    .deliverables-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    .deliverable-item {
      display: flex;
      gap: 14px;
      padding: 14px;
      background: #141c28;
      border: 1px solid #1e293b;
      border-radius: 6px;

      h4 {
        font-size: 0.9rem;
        font-weight: 700;
        color: #f8fafc;
        margin: 0 0 4px 0;
      }

      p {
        font-size: 0.8rem;
        color: #94a3b8;
        margin: 0;
        line-height: 1.4;
      }
    }

    .num-badge {
      width: 32px;
      height: 32px;
      background: rgba(225, 29, 72, 0.15);
      border: 1px solid rgba(225, 29, 72, 0.3);
      color: #fb7185;
      font-weight: 800;
      font-size: 0.8rem;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .badge {
      display: inline-block;
      padding: 3px 8px;
      border-radius: 4px;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
    }

    .badge-warning {
      background: rgba(245, 158, 11, 0.2);
      color: #f59e0b;
      border: 1px solid rgba(245, 158, 11, 0.4);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 8px 16px;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.15s ease;

      &.btn-primary {
        background: linear-gradient(135deg, #e11d48 0%, #be123c 100%);
        color: #ffffff;
      }

      &.btn-secondary {
        background: #1e293b;
        color: #f8fafc;
      }
    }

    .actions {
      display: flex;
      gap: 12px;
    }

    .mt-4 { margin-top: 16px; }
    .text-rose { color: #e11d48; }
    .w-12 { width: 48px; }
    .h-12 { height: 48px; }
  `]
})
export class MatchesHubComponent {}
