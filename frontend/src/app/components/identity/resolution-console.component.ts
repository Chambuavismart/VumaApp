import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IdentityService,
  TeamResolutionResult,
  CompetitionResolutionResult,
  SeasonResolutionResult,
  CompetitionDetail
} from '../../services/identity.service';

@Component({
  selector: 'app-resolution-console',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="resolution-console">
      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="header-badge">INTERACTIVE RESOLUTION CONSOLE</div>
          <h1 class="page-title">Identity Resolution Testing Workbench</h1>
          <p class="page-subtitle">Submit incoming raw vendor data and inspect live resolution status (RESOLVED, UNRESOLVED, AMBIGUOUS, INVALID_INPUT)</p>
        </div>
      </div>

      <!-- Entity Type Switcher -->
      <div class="type-switcher">
        <button class="tab-btn" [class.active]="activeTab === 'team'" (click)="activeTab = 'team'">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          Team Resolver
        </button>
        <button class="tab-btn" [class.active]="activeTab === 'competition'" (click)="activeTab = 'competition'">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          Competition Resolver
        </button>
        <button class="tab-btn" [class.active]="activeTab === 'season'" (click)="activeTab = 'season'">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          Season Resolver
        </button>
      </div>

      <!-- Workbench Grid -->
      <div class="workbench-grid">
        <!-- Input Form -->
        <div class="card form-card">
          <!-- TEAM FORM -->
          <div *ngIf="activeTab === 'team'">
            <h2 class="card-title">Test Team Identity</h2>
            <p class="card-subtitle">Test raw incoming vendor team names against canonical clubs and registered aliases.</p>

            <form (ngSubmit)="testResolveTeam()" class="test-form">
              <div class="form-group">
                <label>Incoming Team Name <span class="text-rose">*</span></label>
                <input type="text" [(ngModel)]="teamInput" name="teamInput" placeholder="e.g. Arsenal FC, Sporting, AS Roma" required class="form-input font-bold" />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Provider / Source (optional)</label>
                  <input type="text" [(ngModel)]="teamProvider" name="teamProvider" placeholder="e.g. football-data" class="form-input" />
                </div>
                <div class="form-group">
                  <label>Country Context (optional)</label>
                  <input type="text" [(ngModel)]="teamCountry" name="teamCountry" placeholder="e.g. England, Spain" class="form-input" />
                </div>
              </div>

              <div class="form-group">
                <label>Competition Context (optional)</label>
                <input type="text" [(ngModel)]="teamComp" name="teamComp" placeholder="e.g. Premier League" class="form-input" />
              </div>

              <button type="submit" [disabled]="resolving" class="btn btn-primary w-full">
                {{ resolving ? 'Resolving...' : 'Execute Team Resolution' }}
              </button>
            </form>
          </div>

          <!-- COMPETITION FORM -->
          <div *ngIf="activeTab === 'competition'">
            <h2 class="card-title">Test Competition Identity</h2>
            <p class="card-subtitle">Test incoming vendor league codes (e.g. E0, E1, D1, I1, SP1) or raw league names.</p>

            <form (ngSubmit)="testResolveCompetition()" class="test-form">
              <div class="form-group">
                <label>Incoming Name or Code <span class="text-rose">*</span></label>
                <input type="text" [(ngModel)]="compInput" name="compInput" placeholder="e.g. E0, English Premier League, Serie A" required class="form-input font-bold" />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Provider / Source (optional)</label>
                  <input type="text" [(ngModel)]="compProvider" name="compProvider" placeholder="e.g. football-data" class="form-input" />
                </div>
                <div class="form-group">
                  <label>Country Context (optional)</label>
                  <input type="text" [(ngModel)]="compCountry" name="compCountry" placeholder="e.g. England, Italy" class="form-input" />
                </div>
              </div>

              <button type="submit" [disabled]="resolving" class="btn btn-primary w-full">
                {{ resolving ? 'Resolving...' : 'Execute Competition Resolution' }}
              </button>
            </form>
          </div>

          <!-- SEASON FORM -->
          <div *ngIf="activeTab === 'season'">
            <h2 class="card-title">Test Season Identity</h2>
            <p class="card-subtitle">Test incoming season representations (2024/25, 2024-2025) or date-based resolution.</p>

            <form (ngSubmit)="testResolveSeason()" class="test-form">
              <div class="form-group">
                <label>Season Representation</label>
                <input type="text" [(ngModel)]="seasonInput" name="seasonInput" placeholder="e.g. 2024/25, 2024-2025, 2024" class="form-input font-bold" />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>Competition ID / Name</label>
                  <input type="text" [(ngModel)]="seasonComp" name="seasonComp" placeholder="e.g. Premier League or ID" class="form-input" />
                </div>
                <div class="form-group">
                  <label>Match Date (YYYY-MM-DD)</label>
                  <input type="date" [(ngModel)]="seasonDate" name="seasonDate" class="form-input" />
                </div>
              </div>

              <button type="submit" [disabled]="resolving" class="btn btn-primary w-full">
                {{ resolving ? 'Resolving...' : 'Execute Season Resolution' }}
              </button>
            </form>
          </div>
        </div>

        <!-- Result Display Panel -->
        <div class="card result-card">
          <div class="result-header">
            <span class="text-xs text-muted font-mono uppercase">Live Engine Outcome</span>
            <div *ngIf="currentStatus" class="status-badge" [ngClass]="'status-' + currentStatus.toLowerCase()">
              {{ currentStatus }}
            </div>
          </div>

          <!-- When Result Available -->
          <div *ngIf="teamResult && activeTab === 'team'" class="result-body">
            <div class="outcome-box">
              <div class="outcome-row">
                <span class="text-muted">Original Input:</span>
                <span class="font-bold text-white">{{ teamResult.originalName }}</span>
              </div>
              <div class="outcome-row" *ngIf="teamResult.normalizedName">
                <span class="text-muted">Normalized:</span>
                <code class="text-rose">{{ teamResult.normalizedName }}</code>
              </div>
              <div class="outcome-row" *ngIf="teamResult.resolvedTeamId">
                <span class="text-muted">Resolved Canonical Team:</span>
                <span class="font-bold text-emerald">{{ teamResult.resolvedTeamName }} (#{{ teamResult.resolvedTeamId }})</span>
              </div>
              <div class="outcome-row" *ngIf="teamResult.matchedAliasId">
                <span class="text-muted">Matched Alias ID:</span>
                <span class="font-mono text-cyan">#{{ teamResult.matchedAliasId }}</span>
              </div>
              <div class="outcome-explanation">
                <strong>Explanation:</strong> {{ teamResult.explanation }}
              </div>
            </div>

            <!-- Candidates (Ambiguous or Resolved) -->
            <div *ngIf="teamResult.candidates && teamResult.candidates.length > 0" class="candidates-box">
              <span class="candidates-title">Candidates Evaluated ({{ teamResult.candidates.length }}):</span>
              <div *ngFor="let c of teamResult.candidates" class="candidate-item">
                <div class="candidate-header">
                  <span class="font-bold text-white">{{ c.name }} (#{{ c.id }})</span>
                  <span class="badge badge-country" *ngIf="c.country">{{ c.country }}</span>
                </div>
                <div class="text-xs text-muted">{{ c.reason }}</div>
              </div>
            </div>
          </div>

          <!-- Competition Result -->
          <div *ngIf="compResult && activeTab === 'competition'" class="result-body">
            <div class="outcome-box">
              <div class="outcome-row">
                <span class="text-muted">Original Input:</span>
                <span class="font-bold text-white">{{ compResult.originalInput }}</span>
              </div>
              <div class="outcome-row" *ngIf="compResult.normalizedInput">
                <span class="text-muted">Normalized:</span>
                <code class="text-rose">{{ compResult.normalizedInput }}</code>
              </div>
              <div class="outcome-row" *ngIf="compResult.resolvedCompetitionId">
                <span class="text-muted">Resolved Competition:</span>
                <span class="font-bold text-emerald">{{ compResult.resolvedCompetitionName }} (#{{ compResult.resolvedCompetitionId }}) - {{ compResult.resolvedCompetitionCountry }}</span>
              </div>
              <div class="outcome-explanation">
                <strong>Explanation:</strong> {{ compResult.explanation }}
              </div>
            </div>

            <div *ngIf="compResult.candidates && compResult.candidates.length > 0" class="candidates-box">
              <span class="candidates-title">Candidates ({{ compResult.candidates.length }}):</span>
              <div *ngFor="let c of compResult.candidates" class="candidate-item">
                <div class="candidate-header">
                  <span class="font-bold text-white">{{ c.name }} (#{{ c.id }})</span>
                  <span class="badge badge-country" *ngIf="c.country">{{ c.country }}</span>
                </div>
                <div class="text-xs text-muted">{{ c.reason }}</div>
              </div>
            </div>
          </div>

          <!-- Season Result -->
          <div *ngIf="seasonResult && activeTab === 'season'" class="result-body">
            <div class="outcome-box">
              <div class="outcome-row">
                <span class="text-muted">Input:</span>
                <span class="font-bold text-white">{{ seasonResult.originalInput }}</span>
              </div>
              <div class="outcome-row" *ngIf="seasonResult.resolvedSeasonCode">
                <span class="text-muted">Canonical Season Code:</span>
                <span class="font-mono font-bold text-emerald">{{ seasonResult.resolvedSeasonCode }}</span>
              </div>
              <div class="outcome-row" *ngIf="seasonResult.resolvedCompetitionSeasonId">
                <span class="text-muted">Comp-Season Link ID:</span>
                <span class="font-mono text-cyan">#{{ seasonResult.resolvedCompetitionSeasonId }}</span>
              </div>
              <div class="outcome-row" *ngIf="seasonResult.startDate && seasonResult.endDate">
                <span class="text-muted">Configured Dates:</span>
                <span class="font-mono text-xs text-cyan">{{ seasonResult.startDate }} &rarr; {{ seasonResult.endDate }}</span>
              </div>
              <div class="outcome-explanation">
                <strong>Explanation:</strong> {{ seasonResult.explanation }}
              </div>
            </div>
          </div>

          <!-- Empty Result State -->
          <div *ngIf="!currentStatus" class="empty-result">
            <svg class="w-12 h-12 text-muted mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            <p class="text-muted">Enter resolution parameters on the left and click execute to query the live identity engine.</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .resolution-console {
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

    .type-switcher {
      display: flex;
      gap: 12px;
      margin-bottom: 24px;
    }

    .tab-btn {
      display: flex;
      align-items: center;
      padding: 10px 18px;
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 8px;
      color: #94a3b8;
      font-weight: 600;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .tab-btn.active {
      background: #1e293b;
      color: #f8fafc;
      border-color: #e11d48;
    }

    .workbench-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;
    }

    @media (max-width: 900px) {
      .workbench-grid {
        grid-template-columns: 1fr;
      }
    }

    .card {
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 12px;
      padding: 24px;
    }

    .card-title {
      font-size: 1.25rem;
      font-weight: 800;
      color: #f8fafc;
      margin: 0 0 4px 0;
    }

    .card-subtitle {
      font-size: 0.85rem;
      color: #94a3b8;
      margin: 0 0 20px 0;
    }

    .test-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
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
      padding: 10px 14px;
      color: #f1f5f9;
      font-size: 0.9rem;
      outline: none;
    }

    .form-input:focus {
      border-color: #e11d48;
    }

    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 14px;
      border-bottom: 1px solid #1e293b;
    }

    .status-badge {
      font-size: 0.8rem;
      font-weight: 800;
      letter-spacing: 0.05em;
      padding: 4px 10px;
      border-radius: 6px;
      text-transform: uppercase;
    }

    .status-resolved { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4); }
    .status-unresolved { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); }
    .status-ambiguous { background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.4); }
    .status-invalid_input { background: rgba(168, 85, 247, 0.2); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.4); }

    .outcome-box {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 8px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-bottom: 20px;
    }

    .outcome-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.875rem;
    }

    .outcome-explanation {
      font-size: 0.85rem;
      color: #94a3b8;
      border-top: 1px solid #1f2937;
      padding-top: 10px;
      margin-top: 4px;
    }

    .candidates-box {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .candidates-title {
      font-size: 0.8rem;
      font-weight: 700;
      color: #94a3b8;
      text-transform: uppercase;
    }

    .candidate-item {
      background: #111827;
      border: 1px solid #1f2937;
      border-radius: 8px;
      padding: 10px 14px;
    }

    .candidate-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 4px;
    }

    .badge-country {
      background: #1e293b;
      color: #38bdf8;
      font-size: 0.75rem;
      padding: 2px 6px;
      border-radius: 4px;
    }

    .empty-result {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 48px 24px;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 10px 18px;
      font-size: 0.875rem;
      font-weight: 600;
      border-radius: 8px;
      border: none;
      cursor: pointer;
    }

    .btn-primary { background: #e11d48; color: #fff; }
    .btn-primary:hover { background: #be123c; }
    .w-full { width: 100%; }
  `]
})
export class ResolutionConsoleComponent {
  activeTab: 'team' | 'competition' | 'season' = 'team';
  resolving = false;
  currentStatus: string | null = null;

  // Team Form
  teamInput = '';
  teamProvider = '';
  teamCountry = '';
  teamComp = '';
  teamResult: TeamResolutionResult | null = null;

  // Competition Form
  compInput = '';
  compProvider = '';
  compCountry = '';
  compResult: CompetitionResolutionResult | null = null;

  // Season Form
  seasonInput = '';
  seasonComp = '';
  seasonDate = '';
  seasonResult: SeasonResolutionResult | null = null;

  constructor(private identityService: IdentityService) {}

  testResolveTeam(): void {
    if (!this.teamInput) return;
    this.resolving = true;
    this.currentStatus = null;
    this.teamResult = null;

    this.identityService.resolveTeam({
      incomingName: this.teamInput,
      provider: this.teamProvider || undefined,
      countryContext: this.teamCountry || undefined,
      competitionContext: this.teamComp || undefined
    }).subscribe({
      next: (res) => {
        this.teamResult = res;
        this.currentStatus = res.status;
        this.resolving = false;
      },
      error: () => {
        this.resolving = false;
      }
    });
  }

  testResolveCompetition(): void {
    if (!this.compInput) return;
    this.resolving = true;
    this.currentStatus = null;
    this.compResult = null;

    this.identityService.resolveCompetition({
      incomingNameOrCode: this.compInput,
      provider: this.compProvider || undefined,
      countryContext: this.compCountry || undefined
    }).subscribe({
      next: (res) => {
        this.compResult = res;
        this.currentStatus = res.status;
        this.resolving = false;
      },
      error: () => {
        this.resolving = false;
      }
    });
  }

  testResolveSeason(): void {
    if (!this.seasonInput && !this.seasonDate) return;
    this.resolving = true;
    this.currentStatus = null;
    this.seasonResult = null;

    this.identityService.resolveSeason({
      incomingSeasonRepresentation: this.seasonInput || undefined,
      competitionNameOrCode: this.seasonComp || undefined,
      matchDate: this.seasonDate || undefined
    }).subscribe({
      next: (res) => {
        this.seasonResult = res;
        this.currentStatus = res.status;
        this.resolving = false;
      },
      error: () => {
        this.resolving = false;
      }
    });
  }
}
