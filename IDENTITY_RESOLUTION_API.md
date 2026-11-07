# Identity Resolution & Alias Management REST API

Base path: `/api/v1/identity`

---

## 1. Overview Statistics
`GET /api/v1/identity/overview`

Returns actual database counts and alias breakdown by provider.

**Response Example (200 OK):**
```json
{
  "totalCanonicalTeams": 20,
  "totalCanonicalCompetitions": 5,
  "totalSeasons": 3,
  "totalCompetitionSeasons": 5,
  "totalTeamAliases": 42,
  "totalCompetitionAliases": 10,
  "teamAliasesBySource": {
    "football-data": 20,
    "opta": 22
  },
  "competitionAliasesBySource": {
    "football-data": 5,
    "opta": 5
  }
}
```

---

## 2. Team Identity Endpoints

### 2.1 Resolve Team
`POST /api/v1/identity/teams/resolve`

**Request Example:**
```json
{
  "incomingName": "Arsenal FC",
  "provider": "football-data",
  "countryContext": "England",
  "competitionContext": "Premier League"
}
```

**Response Example (RESOLVED):**
```json
{
  "status": "RESOLVED",
  "originalName": "Arsenal FC",
  "normalizedName": "arsenal fc",
  "resolvedTeamId": 1,
  "resolvedTeamName": "Arsenal",
  "matchedAliasId": 14,
  "provider": "football-data",
  "countryContext": "england",
  "competitionContext": "premier league",
  "explanation": "Resolved via exact provider-scoped alias mapping",
  "candidates": [
    {
      "id": 1,
      "name": "Arsenal",
      "country": "England",
      "reason": "Resolved via exact provider-scoped alias mapping"
    }
  ]
}
```

**Response Example (AMBIGUOUS):**
```json
{
  "status": "AMBIGUOUS",
  "originalName": "Sporting",
  "normalizedName": "sporting",
  "explanation": "Multiple canonical teams exist with name 'sporting' across different countries. Provide country context.",
  "candidates": [
    {
      "id": 12,
      "name": "Sporting CP",
      "country": "Portugal",
      "reason": "Canonical team in: Portugal"
    },
    {
      "id": 13,
      "name": "Sporting Gijon",
      "country": "Spain",
      "reason": "Canonical team in: Spain"
    }
  ]
}
```

### 2.2 List Canonical Teams
`GET /api/v1/identity/teams?query=Arsenal&page=0&size=20&sortBy=name&sortDirection=asc`

### 2.3 Get Canonical Team Detail
`GET /api/v1/identity/teams/{teamId}`

### 2.4 List Team Aliases
`GET /api/v1/identity/teams/{teamId}/aliases`

### 2.5 Register Team Alias
`POST /api/v1/identity/teams/{teamId}/aliases`

**Request:**
```json
{
  "source": "football-data",
  "rawAlias": "The Gunners",
  "countryContext": "England",
  "competitionContext": "Premier League"
}
```

**Responses:**
- `201 Created`: Alias registered successfully.
- `404 Not Found`: Team does not exist.
- `409 Conflict`: Alias is already mapped to a different canonical team under the same scope.

---

## 3. Competition Identity Endpoints

### 3.1 Resolve Competition
`POST /api/v1/identity/competitions/resolve`

**Request:**
```json
{
  "incomingNameOrCode": "E0",
  "provider": "football-data",
  "countryContext": "England"
}
```

**Response (RESOLVED):**
```json
{
  "status": "RESOLVED",
  "originalInput": "E0",
  "normalizedInput": "e0",
  "resolvedCompetitionId": 1,
  "resolvedCompetitionName": "Premier League",
  "resolvedCompetitionCountry": "England",
  "matchedAliasId": 3,
  "provider": "football-data",
  "countryContext": "england",
  "explanation": "Resolved via provider source code (E0) mapping"
}
```

### 3.2 List Competitions
`GET /api/v1/identity/competitions?query=Premier&page=0&size=20`

### 3.3 Register Competition Alias
`POST /api/v1/identity/competitions/{competitionId}/aliases`

**Request:**
```json
{
  "source": "football-data",
  "sourceCode": "E0",
  "rawAlias": "E0",
  "countryContext": "England"
}
```

---

## 4. Season Identity Endpoints

### 4.1 Resolve Season
`POST /api/v1/identity/seasons/resolve`

**Request:**
```json
{
  "incomingSeasonRepresentation": "2024/25",
  "competitionId": 1,
  "matchDate": "2024-11-10"
}
```

**Response:**
```json
{
  "status": "RESOLVED",
  "originalInput": "2024/25",
  "normalizedSeasonCode": "2024/2025",
  "resolvedSeasonId": 1,
  "resolvedSeasonCode": "2024/2025",
  "resolvedSeasonName": "2024/2025 Season",
  "resolvedCompetitionSeasonId": 1,
  "competitionId": 1,
  "competitionName": "Premier League",
  "startDate": "2024-08-10",
  "endDate": "2025-05-25",
  "explanation": "Resolved via match date (2024-11-10) within competition-season boundaries (2024-08-10 to 2025-05-25)"
}
```

### 4.2 List Seasons
`GET /api/v1/identity/seasons?page=0&size=20`

### 4.3 Get Competition Seasons
`GET /api/v1/identity/competitions/{competitionId}/seasons`
