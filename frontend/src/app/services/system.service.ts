import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, catchError, of, tap, switchMap } from 'rxjs';
import { SystemStatusResponse } from '../models/system-status.model';

@Injectable({
  providedIn: 'root'
})
export class SystemService {
  private primaryUrl = '/api/v1/system/status';
  private directUrl = 'http://localhost:8080/api/v1/system/status';
  
  private statusSubject = new BehaviorSubject<SystemStatusResponse | null>(null);
  public status$ = this.statusSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  public fetchStatus(): Observable<SystemStatusResponse | null> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    // Try relative URL (proxied through Angular CLI) first, fallback to direct port 8080
    return this.http.get<SystemStatusResponse>(this.primaryUrl).pipe(
      catchError(() => this.http.get<SystemStatusResponse>(this.directUrl)),
      tap((status) => {
        this.statusSubject.next(status);
        this.loadingSubject.next(false);
      }),
      catchError((err: any) => {
        const errorMsg = 'Backend is unreachable on http://localhost:8080. Ensure Spring Boot is started.';
        this.errorSubject.next(errorMsg);
        this.loadingSubject.next(false);
        this.statusSubject.next({
          applicationName: 'VumaPhaseTwo',
          applicationVersion: '1.0.0-SNAPSHOT',
          environment: 'Development',
          status: 'DOWN',
          javaVersion: '',
          springBootVersion: '',
          database: {
            status: 'OFFLINE',
            databaseName: 'Vumaapp',
            tableCount: 0,
            pingTimeMs: -1,
            error: errorMsg
          },
          timestamp: new Date().toISOString(),
          details: {}
        });
        return of(null);
      })
    );
  }

  public getActuatorHealth(): Observable<any> {
    return this.http.get<any>('/actuator/health').pipe(
      catchError(() => this.http.get<any>('http://localhost:8080/actuator/health')),
      catchError((err: any) => of({ status: 'DOWN', error: err?.message }))
    );
  }
}
