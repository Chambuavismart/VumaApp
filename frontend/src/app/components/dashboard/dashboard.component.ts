import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardOverview } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DecimalPipe, DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);

  public overview: DashboardOverview | null = null;
  public loading: boolean = true;
  public errorMessage: string | null = null;
  public lastRefreshed: Date = new Date();

  ngOnInit(): void {
    this.loadOverview();
  }

  public loadOverview(): void {
    this.loading = true;
    this.errorMessage = null;
    this.dashboardService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.loading = false;
        this.lastRefreshed = new Date();
      },
      error: (err) => {
        this.errorMessage = 'Unable to connect to Spring Boot backend at http://localhost:8080. Please ensure the backend is running.';
        this.loading = false;
      }
    });
  }

  public getCoveredCompetitionsCount(): number {
    if (!this.overview?.dataCoverage) return 0;
    const comps = new Set(this.overview.dataCoverage.filter(c => c.matchCount > 0).map(c => c.competitionId));
    return comps.size;
  }

  public getTotalMatchesFromCoverage(): number {
    if (!this.overview?.dataCoverage) return 0;
    return this.overview.dataCoverage.reduce((acc, curr) => acc + curr.matchCount, 0);
  }
}
