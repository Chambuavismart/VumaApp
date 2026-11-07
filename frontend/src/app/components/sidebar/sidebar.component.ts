import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface NavSection {
  title: string;
  items: NavItem[];
}

interface NavItem {
  name: string;
  route: string;
  icon: string;
  phase: string;
  status: 'active' | 'upcoming' | 'pending';
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar">
      <!-- Logo & Brand Header -->
      <div class="brand">
        <div class="brand-logo">
          <svg class="w-6 h-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <path d="m4.93 4.93 4.24 4.24"></path>
            <path d="m14.83 9.17 4.24-4.24"></path>
            <path d="m14.83 14.83 4.24 4.24"></path>
            <path d="m9.17 14.83-4.24 4.24"></path>
            <circle cx="12" cy="12" r="4"></circle>
          </svg>
        </div>
        <div class="brand-info">
          <span class="brand-name">VUMA<span class="text-rose">PHASE</span>TWO</span>
          <span class="brand-tag">Football Research Platform</span>
        </div>
      </div>

      <!-- Navigation Sections -->
      <div class="nav-container">
        @for (section of navSections; track section.title) {
          <div class="nav-section">
            <div class="section-title">{{ section.title }}</div>
            <div class="section-items">
              @for (item of section.items; track item.route) {
                <a [routerLink]="item.route" 
                   routerLinkActive="active" 
                   [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }"
                   class="nav-item">
                  <span class="item-icon" [innerHTML]="item.icon"></span>
                  <span class="item-label">{{ item.name }}</span>
                  <span class="phase-pill" [class]="'phase-' + item.status">{{ item.phase }}</span>
                </a>
              }
            </div>
          </div>
        }
      </div>

      <!-- Footer Info -->
      <div class="sidebar-footer">
        <div class="env-box">
          <div class="env-row">
            <span class="text-muted">Database:</span>
            <span class="font-mono text-emerald">Vumaapp</span>
          </div>
          <div class="env-row">
            <span class="text-muted">Target DB:</span>
            <span class="font-mono text-cyan">MySQL 8.0</span>
          </div>
          <div class="env-row">
            <span class="text-muted">Platform Mode:</span>
            <span class="badge badge-success text-xs">RESEARCH LIVE</span>
          </div>
        </div>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 280px;
      height: 100vh;
      background: #0f1520;
      border-right: 1px solid #1e293b;
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      user-select: none;
    }

    .brand {
      padding: 20px 24px;
      display: flex;
      align-items: center;
      gap: 14px;
      border-bottom: 1px solid #1e293b;
      background: #0a0e14;
    }

    .brand-logo {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: linear-gradient(135deg, #e11d48 0%, #9f1239 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(225, 29, 72, 0.35);
      border: 1px solid rgba(255, 255, 255, 0.15);
    }

    .brand-info {
      display: flex;
      flex-direction: column;
    }

    .brand-name {
      font-weight: 800;
      font-size: 1.125rem;
      letter-spacing: -0.02em;
      color: #ffffff;
    }

    .text-rose {
      color: #e11d48;
    }

    .brand-tag {
      font-size: 0.725rem;
      color: #64748b;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .nav-container {
      flex: 1;
      overflow-y: auto;
      padding: 16px 12px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .section-title {
      font-size: 0.675rem;
      font-weight: 700;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      padding: 0 12px 6px 12px;
    }

    .section-items {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 9px 12px;
      border-radius: 6px;
      color: #94a3b8;
      text-decoration: none;
      font-size: 0.85rem;
      font-weight: 500;
      transition: all 0.15s ease;
      border: 1px solid transparent;
    }

    .nav-item:hover {
      background: #141c28;
      color: #f8fafc;
      border-color: #1e293b;
    }

    .nav-item.active {
      background: rgba(225, 29, 72, 0.12);
      color: #ffffff;
      border-color: rgba(225, 29, 72, 0.35);
      font-weight: 600;
    }

    .item-icon {
      width: 18px;
      height: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0.8;
    }

    .nav-item.active .item-icon {
      color: #e11d48;
      opacity: 1;
    }

    .item-label {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .phase-pill {
      font-size: 0.65rem;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 4px;
      text-transform: uppercase;
    }

    .phase-active {
      background: rgba(225, 29, 72, 0.2);
      color: #fb7185;
      border: 1px solid rgba(225, 29, 72, 0.4);
    }

    .phase-upcoming {
      background: #1e293b;
      color: #64748b;
    }

    .sidebar-footer {
      padding: 14px;
      border-top: 1px solid #1e293b;
      background: #0a0e14;
    }

    .env-box {
      background: #141c28;
      border: 1px solid #1e293b;
      border-radius: 6px;
      padding: 10px 12px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      font-size: 0.75rem;
    }

    .env-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .text-emerald { color: #10b981; }
    .text-cyan { color: #06b6d4; }
    .text-muted { color: #64748b; }
    .text-xs { font-size: 0.65rem; }

    .badge {
      display: inline-block;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 0.65rem;
      font-weight: 700;
    }

    .badge-success {
      background: rgba(16, 185, 129, 0.2);
      color: #10b981;
      border: 1px solid rgba(16, 185, 129, 0.4);
    }
  `]
})
export class SidebarComponent {
  navSections: NavSection[] = [
    {
      title: 'FOOTBALL RESEARCH',
      items: [
        {
          name: 'Dashboard',
          route: '/dashboard',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>`,
          phase: 'Live',
          status: 'active'
        },
        {
          name: 'Matches',
          route: '/matches',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>`,
          phase: 'Phase 4',
          status: 'upcoming'
        },
        {
          name: 'Teams',
          route: '/teams',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M22 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>`,
          phase: 'Phase 2',
          status: 'active'
        },
        {
          name: 'Competitions',
          route: '/competitions',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"></path><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"></path><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path></svg>`,
          phase: 'Phase 2',
          status: 'active'
        },
        {
          name: 'Data Centre',
          route: '/data-centre',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>`,
          phase: 'Phase 3',
          status: 'active'
        },
        {
          name: 'Research Lab',
          route: '/research-lab',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 2v7.31L4.69 17.62A2 2 0 0 0 6.1 21h11.8a2 2 0 0 0 1.41-3.38L14 9.31V2"></path><path d="M8.5 2h7"></path><path d="M7 16h10"></path></svg>`,
          phase: 'Lab',
          status: 'active'
        }
      ]
    },
    {
      title: 'PLATFORM CONTROL',
      items: [
        {
          name: 'System & Diagnostics',
          route: '/system-diagnostics',
          icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>`,
          phase: 'P0–P2',
          status: 'active'
        }
      ]
    }
  ];
}
