import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SystemService } from '../../services/system.service';
import { DatabaseExplorerService } from '../../services/database-explorer.service';
import {
  DatabaseOverview,
  TableSummary,
  TableStructure,
  RelationshipDetail,
  MigrationRecord
} from '../../models/database-explorer.model';
import { IdentityService, TeamResolutionResult, CompetitionResolutionResult, SeasonResolutionResult } from '../../services/identity.service';

@Component({
  selector: 'app-system-diagnostics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './system-diagnostics.component.html',
  styleUrls: ['./system-diagnostics.component.scss']
})
export class SystemDiagnosticsComponent implements OnInit {
  public activeTab: 'health' | 'tables' | 'relationships' | 'migrations' | 'resolution' | 'config' = 'health';
  
  public systemService = inject(SystemService);
  public dbService = inject(DatabaseExplorerService);
  public identityService = inject(IdentityService);

  public status: any = null;
  public loadingStatus = false;

  // DB Explorer State
  public loadingOverview = false;
  public loadingTables = false;
  public loadingStructure = false;
  public loadingRelationships = false;
  public loadingMigrations = false;
  public errorMessage: string | null = null;
  public successMessage: string | null = null;

  public overview: DatabaseOverview | null = null;
  public tables: TableSummary[] = [];
  public selectedTableName: string | null = null;
  public selectedTableStructure: TableStructure | null = null;
  public relationships: RelationshipDetail[] = [];
  public migrations: MigrationRecord[] = [];

  // Resolution Tester State
  public testTeamRaw: string = 'Arsenal FC';
  public testTeamCountry: string = 'England';
  public testTeamComp: string = 'Premier League';
  public teamResult: TeamResolutionResult | null = null;
  public resolvingTeam: boolean = false;

  public testCompRaw: string = 'E0';
  public testCompCountry: string = 'England';
  public compResult: CompetitionResolutionResult | null = null;
  public resolvingComp: boolean = false;

  public testSeasonRaw: string = '2024-2025';
  public testSeasonDate: string = '2024-11-15';
  public seasonResult: SeasonResolutionResult | null = null;
  public resolvingSeason: boolean = false;

  ngOnInit(): void {
    this.systemService.status$.subscribe(s => this.status = s);
    this.refreshHealth();
    this.loadDatabaseInfo();
  }

  public refreshHealth(): void {
    this.loadingStatus = true;
    this.systemService.fetchStatus().subscribe({
      next: (data) => {
        this.status = data;
        this.loadingStatus = false;
      },
      error: () => {
        this.loadingStatus = false;
      }
    });
  }

  public loadDatabaseInfo(): void {
    this.loadOverview();
    this.loadTables();
    this.loadRelationships();
    this.loadMigrations();
  }

  public loadOverview(): void {
    this.loadingOverview = true;
    this.dbService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.loadingOverview = false;
      },
      error: () => {
        this.loadingOverview = false;
      }
    });
  }

  public loadTables(): void {
    this.loadingTables = true;
    this.dbService.getTables().subscribe({
      next: (data) => {
        this.tables = data;
        this.loadingTables = false;
        if (data.length > 0 && !this.selectedTableName) {
          this.selectTable(data[0].tableName);
        }
      },
      error: () => {
        this.loadingTables = false;
      }
    });
  }

  public selectTable(tableName: string): void {
    this.selectedTableName = tableName;
    this.loadingStructure = true;
    this.selectedTableStructure = null;
    this.dbService.getTableStructure(tableName).subscribe({
      next: (data) => {
        this.selectedTableStructure = data;
        this.loadingStructure = false;
      },
      error: () => {
        this.loadingStructure = false;
      }
    });
  }

  public loadRelationships(): void {
    this.loadingRelationships = true;
    this.dbService.getRelationships().subscribe({
      next: (data) => {
        this.relationships = data;
        this.loadingRelationships = false;
      },
      error: () => {
        this.loadingRelationships = false;
      }
    });
  }

  public loadMigrations(): void {
    this.loadingMigrations = true;
    this.dbService.getMigrations().subscribe({
      next: (data) => {
        this.migrations = data;
        this.loadingMigrations = false;
      },
      error: () => {
        this.loadingMigrations = false;
      }
    });
  }

  public testResolveTeam(): void {
    if (!this.testTeamRaw) return;
    this.resolvingTeam = true;
    this.teamResult = null;
    this.identityService.resolveTeam({
      incomingName: this.testTeamRaw,
      countryContext: this.testTeamCountry || undefined,
      competitionContext: this.testTeamComp || undefined
    }).subscribe({
      next: (res) => {
        this.teamResult = res;
        this.resolvingTeam = false;
      },
      error: (err) => {
        this.errorMessage = 'Team resolution failed: ' + (err.error?.message || err.message);
        this.resolvingTeam = false;
      }
    });
  }

  public testResolveCompetition(): void {
    if (!this.testCompRaw) return;
    this.resolvingComp = true;
    this.compResult = null;
    this.identityService.resolveCompetition({
      incomingNameOrCode: this.testCompRaw,
      countryContext: this.testCompCountry || undefined
    }).subscribe({
      next: (res) => {
        this.compResult = res;
        this.resolvingComp = false;
      },
      error: (err) => {
        this.errorMessage = 'Competition resolution failed: ' + (err.error?.message || err.message);
        this.resolvingComp = false;
      }
    });
  }

  public testResolveSeason(): void {
    if (!this.testSeasonRaw && !this.testSeasonDate) return;
    this.resolvingSeason = true;
    this.seasonResult = null;
    this.identityService.resolveSeason({
      incomingSeasonRepresentation: this.testSeasonRaw || undefined,
      matchDate: this.testSeasonDate || undefined
    }).subscribe({
      next: (res) => {
        this.seasonResult = res;
        this.resolvingSeason = false;
      },
      error: (err) => {
        this.errorMessage = 'Season resolution failed: ' + (err.error?.message || err.message);
        this.resolvingSeason = false;
      }
    });
  }
}
