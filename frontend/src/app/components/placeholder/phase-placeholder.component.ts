import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-phase-placeholder',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="placeholder-container">
      <div class="card placeholder-card">
        <div class="card-icon">
          <svg class="w-12 h-12 text-rose" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="3" y1="9" x2="21" y2="9"></line>
            <line x1="9" y1="21" x2="9" y2="9"></line>
          </svg>
        </div>
        <div class="phase-badge">
          <span class="badge badge-warning">{{ phaseLabel }}</span>
        </div>
        <h2 class="title">{{ pageTitle }}</h2>
        <p class="description">
          {{ pageDescription }}
        </p>

        <div class="spec-box">
          <div class="spec-header">
            <svg class="w-4 h-4 text-emerald" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            <span>Target Deliverable in Blueprint Roadmap</span>
          </div>
          <ul class="spec-list">
            @for (item of deliverableItems; track item) {
              <li>{{ item }}</li>
            }
          </ul>
        </div>

        <div class="action-row">
          <a routerLink="/dashboard" class="btn-secondary">
            ← Return to Dashboard
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .placeholder-container {
      padding: 48px 24px;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 120px);
    }

    .placeholder-card {
      max-width: 680px;
      width: 100%;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 36px 32px;
      background: #141c28;
      border: 1px solid #1e293b;
      border-top: 3px solid #e11d48;
    }

    .card-icon {
      width: 64px;
      height: 64px;
      border-radius: 14px;
      background: rgba(225, 29, 72, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid rgba(225, 29, 72, 0.25);
    }

    .title {
      font-size: 1.4rem;
      font-weight: 800;
      color: #ffffff;
      letter-spacing: -0.01em;
    }

    .description {
      font-size: 0.9rem;
      color: #94a3b8;
      line-height: 1.6;
    }

    .spec-box {
      width: 100%;
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 6px;
      padding: 16px 20px;
      text-align: left;
      margin-top: 8px;
    }

    .spec-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.8rem;
      font-weight: 700;
      color: #f8fafc;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 10px;
    }

    .spec-list {
      list-style-type: disc;
      padding-left: 20px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      font-size: 0.825rem;
      color: #94a3b8;
    }

    .action-row {
      margin-top: 12px;
    }

    .text-rose { color: #e11d48; }
    .text-emerald { color: #10b981; }
  `]
})
export class PhasePlaceholderComponent implements OnInit {
  private route = inject(ActivatedRoute);

  pageTitle = 'Module Scheduled in Subsequent Phase';
  phaseLabel = 'Roadmap Phase';
  pageDescription = 'This module will be fully implemented and connected to real MySQL database tables in its designated phase.';
  deliverableItems: string[] = [];

  ngOnInit() {
    this.route.data.subscribe(data => {
      if (data['title']) this.pageTitle = data['title'];
      if (data['phase']) this.phaseLabel = data['phase'];
      if (data['description']) this.pageDescription = data['description'];
      if (data['deliverables']) this.deliverableItems = data['deliverables'];
    });
  }
}
