import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import {
  DatabaseOverview,
  TableSummary,
  TableStructure,
  RelationshipDetail,
  MigrationRecord
} from '../models/database-explorer.model';

@Injectable({
  providedIn: 'root'
})
export class DatabaseExplorerService {
  private primaryBase = '/api/v1/database';
  private directBase = 'http://localhost:8080/api/v1/database';

  constructor(private http: HttpClient) {}

  public getOverview(): Observable<DatabaseOverview> {
    return this.http.get<DatabaseOverview>(`${this.primaryBase}/overview`).pipe(
      catchError(() => this.http.get<DatabaseOverview>(`${this.directBase}/overview`))
    );
  }

  public getTables(): Observable<TableSummary[]> {
    return this.http.get<TableSummary[]>(`${this.primaryBase}/tables`).pipe(
      catchError(() => this.http.get<TableSummary[]>(`${this.directBase}/tables`))
    );
  }

  public getTableStructure(tableName: string): Observable<TableStructure> {
    return this.http.get<TableStructure>(`${this.primaryBase}/tables/${tableName}`).pipe(
      catchError(() => this.http.get<TableStructure>(`${this.directBase}/tables/${tableName}`))
    );
  }

  public getRelationships(): Observable<RelationshipDetail[]> {
    return this.http.get<RelationshipDetail[]>(`${this.primaryBase}/relationships`).pipe(
      catchError(() => this.http.get<RelationshipDetail[]>(`${this.directBase}/relationships`))
    );
  }

  public getMigrations(): Observable<MigrationRecord[]> {
    return this.http.get<MigrationRecord[]>(`${this.primaryBase}/migrations`).pipe(
      catchError(() => this.http.get<MigrationRecord[]>(`${this.directBase}/migrations`))
    );
  }
}
