import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SystemService } from '../../services/system.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="settings-page">
      <div class="card">
        <div class="card-header">
          <h2 class="title">System Configuration & Connection Parameters</h2>
          <span class="badge badge-info">VumaPhaseTwo v1.0.0</span>
        </div>

        <div class="settings-section">
          <h3 class="section-title">Database Endpoint Parameters</h3>
          <div class="params-table">
            <div class="param-row">
              <span class="param-label">Target Database</span>
              <span class="param-val font-mono text-emerald">MySQL 8.0 — Vumaapp</span>
            </div>
            <div class="param-row">
              <span class="param-label">JDBC URL</span>
              <span class="param-val font-mono">jdbc:mysql://localhost:3306/Vumaapp</span>
            </div>
            <div class="param-row">
              <span class="param-label">Authentication</span>
              <span class="param-val font-mono">root / root (Configurable via ENV)</span>
            </div>
            <div class="param-row">
              <span class="param-label">Flyway Migrations</span>
              <span class="param-val font-mono text-amber">classpath:db/migration (Managed)</span>
            </div>
            <div class="param-row">
              <span class="param-label">Hibernate DDL Auto</span>
              <span class="param-val font-mono text-cyan">validate / none (Safe Schema Mode)</span>
            </div>
          </div>
        </div>

        <div class="settings-section">
          <h3 class="section-title">Frontend & API Endpoints</h3>
          <div class="params-table">
            <div class="param-row">
              <span class="param-label">Frontend Dev Server</span>
              <span class="param-val font-mono">http://localhost:4200</span>
            </div>
            <div class="param-row">
              <span class="param-label">Backend API Server</span>
              <span class="param-val font-mono">http://localhost:8080</span>
            </div>
            <div class="param-row">
              <span class="param-label">Actuator Health Check</span>
              <span class="param-val font-mono">http://localhost:8080/actuator/health</span>
            </div>
            <div class="param-row">
              <span class="param-label">System Diagnostics API</span>
              <span class="param-val font-mono">http://localhost:8080/api/v1/system/status</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-page {
      padding: 28px;
      max-width: 1000px;
      margin: 0 auto;
      width: 100%;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #1e293b;
    }

    .title {
      font-size: 1.25rem;
      font-weight: 800;
      color: #ffffff;
    }

    .settings-section {
      margin-bottom: 24px;
    }

    .section-title {
      font-size: 0.85rem;
      font-weight: 700;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 12px;
    }

    .params-table {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .param-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 14px;
      background: #0f1520;
      border: 1px solid #1e293b;
      border-radius: 6px;
      font-size: 0.85rem;
    }

    .param-label {
      color: #94a3b8;
    }

    .param-val {
      color: #f8fafc;
      font-weight: 500;
    }

    .text-emerald { color: #10b981; }
    .text-amber { color: #f59e0b; }
    .text-cyan { color: #06b6d4; }
  `]
})
export class SettingsComponent {
  systemService = inject(SystemService);
}
