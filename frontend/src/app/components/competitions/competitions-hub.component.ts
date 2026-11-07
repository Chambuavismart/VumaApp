import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompetitionExplorerComponent } from '../identity/competition-explorer.component';
import { SeasonExplorerComponent } from '../identity/season-explorer.component';

@Component({
  selector: 'app-competitions-hub',
  standalone: true,
  imports: [CommonModule, CompetitionExplorerComponent, SeasonExplorerComponent],
  template: `
    <div class="competitions-hub">
      <div class="hub-header">
        <div class="header-titles">
          <div class="header-badge">CANONICAL COMPETITIONS &amp; SEASONS</div>
          <h1 class="page-title">Competitions &amp; Season Calendars</h1>
          <p class="subtitle">Inspect canonical league hierarchies, manage Football-Data provider codes (E0, SP1, D1, I1, etc.), and verify season boundary dates.</p>
        </div>
      </div>

      <div class="nav-tabs">
        <button [class.active]="activeTab === 'competitions'" (click)="activeTab = 'competitions'">
          <i class="icon">🏆</i> Competitions &amp; Source Codes
        </button>
        <button [class.active]="activeTab === 'seasons'" (click)="activeTab = 'seasons'">
          <i class="icon">📅</i> Seasons &amp; Calendar Boundaries
        </button>
      </div>

      <div class="hub-content">
        <app-competition-explorer *ngIf="activeTab === 'competitions'"></app-competition-explorer>
        <app-season-explorer *ngIf="activeTab === 'seasons'"></app-season-explorer>
      </div>
    </div>
  `,
  styles: [`
    .competitions-hub {
      padding: 0 0 24px 0;
      width: 100%;
    }

    .hub-header {
      padding: 24px 32px 16px 32px;
      border-bottom: 1px solid #1e293b;
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

    .subtitle {
      color: #94a3b8;
      font-size: 0.875rem;
      margin: 0;
    }

    .nav-tabs {
      display: flex;
      gap: 8px;
      padding: 12px 32px;
      border-bottom: 1px solid #1e293b;
      background: #0f1520;

      button {
        background: transparent;
        border: 1px solid transparent;
        color: #94a3b8;
        padding: 8px 16px;
        font-size: 0.85rem;
        font-weight: 600;
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 8px;
        transition: all 0.15s ease;

        &:hover {
          color: #f8fafc;
          background: #141c28;
        }

        &.active {
          background: rgba(225, 29, 72, 0.15);
          color: #ffffff;
          border-color: rgba(225, 29, 72, 0.4);
        }
      }
    }

    .hub-content {
      /* Padding is handled internally by explorer components */
    }
  `]
})
export class CompetitionsHubComponent {
  activeTab: 'competitions' | 'seasons' = 'competitions';
}
