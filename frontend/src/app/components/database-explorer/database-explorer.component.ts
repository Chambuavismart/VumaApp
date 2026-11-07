import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DatabaseExplorerService } from '../../services/database-explorer.service';
import {
  DatabaseOverview,
  TableSummary,
  TableStructure,
  RelationshipDetail,
  MigrationRecord
} from '../../models/database-explorer.model';

@Component({
  selector: 'app-database-explorer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './database-explorer.component.html',
  styleUrls: ['./database-explorer.component.scss']
})
export class DatabaseExplorerComponent implements OnInit {
  public activeTab: 'tables' | 'relationships' | 'migrations' = 'tables';
  public loadingOverview = false;
  public loadingTables = false;
  public loadingStructure = false;
  public loadingRelationships = false;
  public loadingMigrations = false;
  public errorMessage: string | null = null;

  public overview: DatabaseOverview | null = null;
  public tables: TableSummary[] = [];
  public selectedTableName: string | null = null;
  public selectedTableStructure: TableStructure | null = null;
  public relationships: RelationshipDetail[] = [];
  public migrations: MigrationRecord[] = [];

  constructor(private dbService: DatabaseExplorerService) {}

  ngOnInit(): void {
    this.refreshAll();
  }

  public refreshAll(): void {
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
      error: (err) => {
        this.errorMessage = 'Failed to load database overview. Is backend running?';
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
      error: (err) => {
        this.errorMessage = 'Failed to load tables list';
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
      error: (err) => {
        this.errorMessage = `Failed to load structure for table ${tableName}`;
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
      error: (err) => {
        this.errorMessage = 'Failed to load foreign key relationships';
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
      error: (err) => {
        this.errorMessage = 'Failed to load Flyway migration history';
        this.loadingMigrations = false;
      }
    });
  }
}
