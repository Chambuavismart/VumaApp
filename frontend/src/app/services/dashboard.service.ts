import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardOverview } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl = 'http://localhost:8080/api/v1/dashboard';

  constructor(private http: HttpClient) {}

  getOverview(): Observable<DashboardOverview> {
    return this.http.get<DashboardOverview>(`${this.baseUrl}/overview`);
  }
}
