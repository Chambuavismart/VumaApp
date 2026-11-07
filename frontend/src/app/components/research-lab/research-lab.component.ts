import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-research-lab',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="research-page">
      <div class="page-header">
        <div>
          <div class="header-badge">FOOTBALL RESEARCH LAB</div>
          <h1 class="page-title">Research Lab &amp; Analytical Roadmap</h1>
          <p class="page-subtitle">Centralized research environment for statistical analysis, historical modeling, and data exploration</p>
        </div>
      </div>

      <!-- Active Research Tools Section -->
      <div class="section-card">
        <div class="card-header">
          <div>
            <span class="badge-tag">READY &amp; OPERATIONAL</span>
            <h2 class="section-heading">Active Football Research Engines</h2>
          </div>
        </div>

        <div class="tools-grid">
          <div class="tool-card active-tool">
            <div class="tool-icon text-cyan">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div class="tool-body">
              <div class="tool-top">
                <h3 class="tool-title">Canonical Team &amp; Club Identity</h3>
                <span class="badge badge-success">Phase 2 Live</span>
              </div>
              <p class="tool-desc">Permanent identity resolution matching club names across disparate provider variations and historical naming conventions.</p>
              <a routerLink="/teams" class="btn btn-secondary btn-sm mt-3">Access Teams &rarr;</a>
            </div>
          </div>

          <div class="tool-card active-tool">
            <div class="tool-icon text-emerald">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path>
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path>
                <path d="M4 22h16"></path>
              </svg>
            </div>
            <div class="tool-body">
              <div class="tool-top">
                <h3 class="tool-title">Competition &amp; Season Registry</h3>
                <span class="badge badge-success">Phase 2 Live</span>
              </div>
              <p class="tool-desc">Verified league configurations with boundary-accurate season calendars avoiding multi-year season guesswork.</p>
              <a routerLink="/competitions" class="btn btn-secondary btn-sm mt-3">Access Competitions &rarr;</a>
            </div>
          </div>

          <div class="tool-card active-tool">
            <div class="tool-icon text-rose">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
            </div>
            <div class="tool-body">
              <div class="tool-top">
                <h3 class="tool-title">Stacked Multi-Season Ingestion</h3>
                <span class="badge badge-success">Phase 3 Live</span>
              </div>
              <p class="tool-desc">Stacked CSV parsing and season separation with deterministic natural keys and quarantine retry workflows.</p>
              <a routerLink="/data-centre" class="btn btn-secondary btn-sm mt-3">Access Data Centre &rarr;</a>
            </div>
          </div>
        </div>
      </div>

      <!-- Analytical Roadmap Section -->
      <div class="section-card mt-4">
        <div class="card-header">
          <div>
            <span class="badge-tag">ROADMAP</span>
            <h2 class="section-heading">Analytical Capabilities Roadmap</h2>
            <p class="section-sub">Scheduled modules awaiting respective development phases before activation.</p>
          </div>
        </div>

        <div class="roadmap-grid">
          <div class="roadmap-card">
            <div class="roadmap-header">
              <span class="phase-badge">Phase 4</span>
              <span class="badge badge-warning">Awaiting Authorization</span>
            </div>
            <h3 class="roadmap-title">Historical Match Retrieval Engine</h3>
            <p class="roadmap-desc">High-speed indexed union queries, 6 query archetypes, H2H matrix calculator, point-in-time as-of-date cutoff.</p>
          </div>

          <div class="roadmap-card">
            <div class="roadmap-header">
              <span class="phase-badge">Phase 5</span>
              <span class="badge badge-secondary">Upcoming</span>
            </div>
            <h3 class="roadmap-title">Automated Data Quality Inspector</h3>
            <p class="roadmap-desc">Post-import data integrity rules, score contradiction detectors (HT > FT), odds bound anomaly checks.</p>
          </div>

          <div class="roadmap-card">
            <div class="roadmap-header">
              <span class="phase-badge">Phase 6 &amp; 7</span>
              <span class="badge badge-secondary">Planned</span>
            </div>
            <h3 class="roadmap-title">Statistical Benchmarks &amp; Multi-League Archive</h3>
            <p class="roadmap-desc">Performance benchmarking suite, large-scale multi-season backfill coordinator, and master historical dataset catalogue.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .research-page {
      padding: 24px 32px;
      max-width: 1400px;
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

    .section-card {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 8px;
      padding: 24px;
      margin-top: 24px;
    }

    .card-header {
      border-bottom: 1px solid #1e293b;
      padding-bottom: 14px;
      margin-bottom: 18px;
    }

    .badge-tag {
      font-size: 0.675rem;
      font-weight: 700;
      color: #e11d48;
      letter-spacing: 0.06em;
      display: block;
      margin-bottom: 4px;
    }

    .section-heading {
      font-size: 1.15rem;
      font-weight: 800;
      color: #f8fafc;
      margin: 0;
    }

    .section-sub {
      font-size: 0.8rem;
      color: #94a3b8;
      margin: 4px 0 0 0;
    }

    .tools-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
      gap: 16px;
    }

    .tool-card {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 18px;
      background: #141c28;
      border: 1px solid #1e293b;
      border-radius: 8px;

      .tool-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;
      }

      .tool-title {
        font-size: 0.95rem;
        font-weight: 700;
        color: #f8fafc;
        margin: 0;
      }

      .tool-desc {
        font-size: 0.825rem;
        color: #94a3b8;
        margin: 0;
        line-height: 1.4;
      }
    }

    .roadmap-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
    }

    .roadmap-card {
      padding: 18px;
      background: #141c28;
      border: 1px solid #1e293b;
      border-radius: 8px;

      .roadmap-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
      }

      .phase-badge {
        font-size: 0.75rem;
        font-weight: 800;
        color: #f8fafc;
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      }

      .roadmap-title {
        font-size: 0.95rem;
        font-weight: 700;
        color: #f8fafc;
        margin: 0 0 6px 0;
      }

      .roadmap-desc {
        font-size: 0.8rem;
        color: #94a3b8;
        margin: 0;
        line-height: 1.4;
      }
    }

    .badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
    }

    .badge-success { background: rgba(16, 185, 129, 0.2); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.4); }
    .badge-warning { background: rgba(245, 158, 11, 0.2); color: #f59e0b; border: 1px solid rgba(245, 158, 11, 0.4); }
    .badge-secondary { background: #1e293b; color: #64748b; }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.15s ease;

      &.btn-secondary {
        background: #1e293b;
        color: #f8fafc;
        border: 1px solid #334155;

        &:hover {
          background: #334155;
          color: #ffffff;
        }
      }
    }

    .text-rose { color: #e11d48; }
    .text-cyan { color: #06b6d4; }
    .text-emerald { color: #10b981; }
    .mt-3 { margin-top: 12px; }
    .mt-4 { margin-top: 16px; }
  `]
})
export class ResearchLabComponent {}
