# VumaPhaseTwo — Historical Football Data Engine & Retrieval Platform

VumaPhaseTwo is a high-performance, normalized historical football database and retrieval system built with Spring Boot 3.4 (Java 21) and Angular 18 (TypeScript).

---

## Architecture Overview

- **Backend:** Java 21, Spring Boot 3.4.2, Spring Data JPA, Spring JDBC, Spring Boot Actuator, Flyway.
- **Database:** Dedicated MySQL 8.0 instance (`Vumaapp` on `localhost:3306`), root / root (configurable via environment variables).
- **Frontend:** Angular 18 with Standalone Components, TypeScript, Angular Router, HttpClient, and modern Dark Football Analytics UI.

---

## Quick Start Guide

### 1. Backend Startup

Ensure MySQL 8.0 is running on port 3306 with the `Vumaapp` database.

```powershell
# From project root
.\mvnw.cmd spring-boot:run
```

Backend will be available on `http://localhost:8080`.

- Health Check: `http://localhost:8080/actuator/health`
- System Status API: `http://localhost:8080/api/v1/system/status`
- Ping API: `http://localhost:8080/api/v1/system/ping`

### 2. Frontend Startup

```powershell
# From frontend directory
cd frontend
npm start
```

Frontend application shell will be accessible at `http://localhost:4200`.

### 3. Automated Tests

```powershell
# Run backend test suite
.\mvnw.cmd test

# Build frontend production bundle
cd frontend
npm run build
```

---

## Phase Status

- **Phase 0: Project Inspection & Development Environment** — COMPLETED & VERIFIED
- **Phase 1: Database Foundation & Live Database Explorer** — COMPLETED & VERIFIED
- **Phase 2: Master Identity Engine & Alias Resolution** — PENDING (Awaiting Authorization)
- **Phase 3: Ingestion Pipeline & Quarantine** — PENDING
- **Phase 4: Match Retrieval & Query Engine** — PENDING
- **Phase 5: Data Quality Dashboard & QC** — PENDING
- **Phase 6: Automated Testing & Verification Centre** — PENDING
- **Phase 7: Historical Backfill & Master Reconciliation** — PENDING
