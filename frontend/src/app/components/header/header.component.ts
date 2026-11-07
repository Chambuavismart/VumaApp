import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SystemService } from '../../services/system.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="header">
      <div class="header-left">
        <h1 class="page-title">VumaPhaseTwo — Football Research &amp; Analytics</h1>
        <span class="badge badge-info">Phase 3.1 Live</span>
      </div>

      <div class="header-right">
        @if (status && status.status === 'HEALTHY') {
          <!-- Backend Status Badge (Online) -->
          <div class="status-indicator status-online">
            <div class="pulse-dot bg-emerald"></div>
            <span class="status-text font-mono">
              SPRING BOOT {{ status.springBootVersion }} [ONLINE]
            </span>
          </div>

          <!-- Database Status Badge (Online) -->
          <div class="status-indicator status-online">
            <svg class="w-4 h-4 text-emerald" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
              <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
              <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
            </svg>
            <span class="status-text font-mono">
              MYSQL: {{ status.database?.databaseName }} ({{ status.database?.pingTimeMs }}ms)
            </span>
          </div>
        } @else {
          <!-- Backend Status Badge (Offline) -->
          <div class="status-indicator status-offline">
            <div class="pulse-dot bg-rose"></div>
            <span class="status-text font-mono">BACKEND [OFFLINE - PORT 8080]</span>
          </div>

          <!-- Database Status Badge (Offline) -->
          <div class="status-indicator status-offline">
            <div class="pulse-dot bg-rose"></div>
            <span class="status-text font-mono">MYSQL: DISCONNECTED</span>
          </div>
        }

        <!-- Refresh Button -->
        <button (click)="refresh()" class="btn-refresh" [disabled]="systemService.loading$ | async" title="Refresh Live Status">
          <svg class="w-4 h-4" [class.animate-spin]="systemService.loading$ | async" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"></polyline>
            <polyline points="1 20 1 14 7 14"></polyline>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
          </svg>
          <span>Refresh</span>
        </button>
      </div>
    </header>
  `,
  styles: [`
    .header {
      height: 64px;
      background: #0f1520;
      border-bottom: 1px solid #1e293b;
      padding: 0 28px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-shrink: 0;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .page-title {
      font-size: 1.05rem;
      font-weight: 700;
      color: #f8fafc;
      letter-spacing: -0.01em;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.775rem;
      font-weight: 600;
      border: 1px solid #1e293b;
      background: #141c28;
    }

    .status-online {
      border-color: rgba(16, 185, 129, 0.3);
      color: #10b981;
    }

    .status-offline {
      border-color: rgba(225, 29, 72, 0.4);
      color: #f43f5e;
    }

    .bg-emerald { background-color: #10b981; }
    .bg-rose { background-color: #e11d48; }
    .text-emerald { color: #10b981; }

    .btn-refresh {
      background: #141c28;
      border: 1px solid #1e293b;
      color: #94a3b8;
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.15s ease;
    }

    .btn-refresh:hover:not(:disabled) {
      background: #1e293b;
      color: #ffffff;
      border-color: #334155;
    }

    .btn-refresh:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .animate-spin {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `]
})
export class HeaderComponent {
  systemService = inject(SystemService);
  status: any = null;

  constructor() {
    this.systemService.status$.subscribe(s => this.status = s);
  }

  refresh() {
    this.systemService.fetchStatus().subscribe();
  }
}
