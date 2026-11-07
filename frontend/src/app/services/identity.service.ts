import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IdentityOverview {
  totalCanonicalTeams: number;
  totalCanonicalCompetitions: number;
  totalSeasons: number;
  totalCompetitionSeasons: number;
  totalTeamAliases: number;
  totalCompetitionAliases: number;
  teamAliasesBySource: { [key: string]: number };
  competitionAliasesBySource: { [key: string]: number };
}

export interface CanonicalCandidate {
  id: number;
  name: string;
  country?: string;
  reason?: string;
}

export interface TeamResolutionResult {
  status: 'RESOLVED' | 'UNRESOLVED' | 'AMBIGUOUS' | 'INVALID_INPUT';
  originalName: string;
  normalizedName?: string;
  resolvedTeamId?: number;
  resolvedTeamName?: string;
  matchedAliasId?: number;
  provider?: string;
  countryContext?: string;
  competitionContext?: string;
  explanation: string;
  candidates?: CanonicalCandidate[];
}

export interface CompetitionResolutionResult {
  status: 'RESOLVED' | 'UNRESOLVED' | 'AMBIGUOUS' | 'INVALID_INPUT';
  originalInput: string;
  normalizedInput?: string;
  resolvedCompetitionId?: number;
  resolvedCompetitionName?: string;
  resolvedCompetitionCountry?: string;
  matchedAliasId?: number;
  provider?: string;
  countryContext?: string;
  explanation: string;
  candidates?: CanonicalCandidate[];
}

export interface SeasonResolutionResult {
  status: 'RESOLVED' | 'UNRESOLVED' | 'AMBIGUOUS' | 'INVALID_INPUT';
  originalInput: string;
  normalizedSeasonCode?: string;
  resolvedSeasonId?: number;
  resolvedSeasonCode?: string;
  resolvedSeasonName?: string;
  resolvedCompetitionSeasonId?: number;
  competitionId?: number;
  competitionName?: string;
  startDate?: string;
  endDate?: string;
  explanation: string;
  candidates?: CanonicalCandidate[];
}

export interface TeamAlias {
  id: number;
  teamId: number;
  teamName: string;
  source: string;
  rawAlias: string;
  normalizedAlias: string;
  countryContext?: string;
  competitionContext?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TeamDetail {
  id: number;
  name: string;
  normalizedName: string;
  country: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  aliases: TeamAlias[];
}

export interface CompetitionAlias {
  id: number;
  competitionId: number;
  competitionName: string;
  source: string;
  sourceCode?: string;
  rawAlias: string;
  normalizedAlias: string;
  countryContext?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CompetitionSeason {
  id: number;
  competitionId: number;
  competitionName: string;
  seasonId: number;
  seasonCode: string;
  seasonName: string;
  startDate: string;
  endDate: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CompetitionDetail {
  id: number;
  name: string;
  normalizedName: string;
  country: string;
  competitionType: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  aliases: CompetitionAlias[];
  seasons: CompetitionSeason[];
}

export interface SeasonDetail {
  id: number;
  seasonCode: string;
  seasonName: string;
  defaultStartDate: string;
  defaultEndDate: string;
  createdAt: string;
  updatedAt: string;
  competitionSeasons: CompetitionSeason[];
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class IdentityService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/identity';

  constructor(private http: HttpClient) {}

  getOverview(): Observable<IdentityOverview> {
    return this.http.get<IdentityOverview>(`${this.baseUrl}/overview`);
  }

  // --- TEAMS ---
  listTeams(query?: string, page = 0, size = 20): Observable<PageResult<TeamDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    return this.http.get<PageResult<TeamDetail>>(`${this.baseUrl}/teams`, { params });
  }

  getTeam(teamId: number): Observable<TeamDetail> {
    return this.http.get<TeamDetail>(`${this.baseUrl}/teams/${teamId}`);
  }

  getTeamAliases(teamId: number): Observable<TeamAlias[]> {
    return this.http.get<TeamAlias[]>(`${this.baseUrl}/teams/${teamId}/aliases`);
  }

  registerTeamAlias(teamId: number, data: { source: string; rawAlias: string; countryContext?: string; competitionContext?: string }): Observable<TeamAlias> {
    return this.http.post<TeamAlias>(`${this.baseUrl}/teams/${teamId}/aliases`, data);
  }

  resolveTeam(data: { incomingName: string; provider?: string; countryContext?: string; competitionContext?: string }): Observable<TeamResolutionResult> {
    return this.http.post<TeamResolutionResult>(`${this.baseUrl}/teams/resolve`, data);
  }

  // --- COMPETITIONS ---
  listCompetitions(query?: string, page = 0, size = 20): Observable<PageResult<CompetitionDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    return this.http.get<PageResult<CompetitionDetail>>(`${this.baseUrl}/competitions`, { params });
  }

  getCompetition(compId: number): Observable<CompetitionDetail> {
    return this.http.get<CompetitionDetail>(`${this.baseUrl}/competitions/${compId}`);
  }

  getCompetitionAliases(compId: number): Observable<CompetitionAlias[]> {
    return this.http.get<CompetitionAlias[]>(`${this.baseUrl}/competitions/${compId}/aliases`);
  }

  registerCompetitionAlias(compId: number, data: { source: string; rawAlias: string; sourceCode?: string; countryContext?: string }): Observable<CompetitionAlias> {
    return this.http.post<CompetitionAlias>(`${this.baseUrl}/competitions/${compId}/aliases`, data);
  }

  getCompetitionSeasons(compId: number): Observable<CompetitionSeason[]> {
    return this.http.get<CompetitionSeason[]>(`${this.baseUrl}/competitions/${compId}/seasons`);
  }

  resolveCompetition(data: { incomingNameOrCode: string; provider?: string; countryContext?: string }): Observable<CompetitionResolutionResult> {
    return this.http.post<CompetitionResolutionResult>(`${this.baseUrl}/competitions/resolve`, data);
  }

  // --- SEASONS ---
  listSeasons(query?: string, page = 0, size = 20): Observable<PageResult<SeasonDetail>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    return this.http.get<PageResult<SeasonDetail>>(`${this.baseUrl}/seasons`, { params });
  }

  getSeason(seasonId: number): Observable<SeasonDetail> {
    return this.http.get<SeasonDetail>(`${this.baseUrl}/seasons/${seasonId}`);
  }

  resolveSeason(data: { incomingSeasonRepresentation?: string; competitionId?: number; competitionNameOrCode?: string; matchDate?: string }): Observable<SeasonResolutionResult> {
    return this.http.post<SeasonResolutionResult>(`${this.baseUrl}/seasons/resolve`, data);
  }
}
