# VumaPhaseTwo — REST API Documentation

**Base URL:** `http://localhost:8080/api/v1`  
**Frontend URL:** `http://localhost:4200`  
**CORS Policy:** Allowed origins: `http://localhost:4200`, `http://127.0.0.1:4200`, `http://localhost:8080` (with credentials).

---

## 1. System Endpoints (`/api/v1/system`)

### `GET /api/v1/system/status`
Returns live system health, JVM runtime metrics, environment profile, and MySQL database connection diagnostics.

**Response `200 OK`:**
```json
{
  "applicationName": "VumaPhaseTwo",
  "applicationVersion": "1.0.0-SNAPSHOT",
  "environment": "Development",
  "status": "HEALTHY",
  "javaVersion": "21.0.12",
  "springBootVersion": "3.4.2",
  "database": {
    "status": "ONLINE",
    "databaseName": "vumaapp",
    "databaseProductName": "MySQL",
    "databaseProductVersion": "8.0.46",
    "connectionUrl": "jdbc:mysql://localhost:3306/Vumaapp?...",
    "tableCount": 11,
    "pingTimeMs": 15,
    "error": null
  },
  "timestamp": "2026-11-07T09:16:24.373Z",
  "details": {
    "availableProcessors": 4,
    "freeMemoryMb": 25,
    "totalMemoryMb": 75,
    "osName": "Windows 11",
    "osArch": "amd64"
  }
}
```

### `GET /api/v1/system/ping`
Lightweight health ping returning `"pong"`.

---

## 2. Database Foundation Explorer Endpoints (`/api/v1/database`)

### `GET /api/v1/database/overview`
Returns live metadata summary of the MySQL `Vumaapp` schema.

**Response `200 OK`:**
```json
{
  "databaseName": "vumaapp",
  "databaseEngine": "MySQL",
  "databaseVersion": "8.0.46",
  "connectionStatus": "ONLINE",
  "totalTables": 11,
  "totalColumns": 141,
  "totalForeignKeys": 15,
  "totalIndexes": 42,
  "flywayStatus": "SUCCESS",
  "latestAppliedMigration": "7 - create indexes",
  "characterSet": "utf8mb4",
  "collation": "utf8mb4_0900_ai_ci"
}
```

### `GET /api/v1/database/tables`
Returns summary statistics for all tables in the active schema.

**Response `200 OK`:**
```json
[
  {
    "number": 1,
    "tableName": "competitions",
    "tableType": "BASE TABLE",
    "approximateRowCount": 0,
    "columnCount": 8,
    "indexCount": 2,
    "foreignKeyCount": 0
  },
  {
    "number": 5,
    "tableName": "historical_matches",
    "tableType": "BASE TABLE",
    "approximateRowCount": 0,
    "columnCount": 41,
    "indexCount": 9,
    "foreignKeyCount": 5
  }
]
```

### `GET /api/v1/database/tables/{tableName}`
Returns detailed column definitions, data types, nullability, constraints, foreign key references, and index membership.

**Parameters:**
- `tableName` (path, required) — Table name to inspect (alphanumeric + underscore only).

**Response `200 OK`:**
```json
{
  "tableName": "historical_matches",
  "columns": [
    {
      "position": 1,
      "columnName": "id",
      "dataType": "BIGINT",
      "nullable": false,
      "primaryKey": true,
      "foreignKey": false,
      "defaultValue": null,
      "indexed": true,
      "referencedTable": null,
      "referencedColumn": null
    },
    {
      "position": 5,
      "columnName": "home_team_id",
      "dataType": "BIGINT",
      "nullable": false,
      "primaryKey": false,
      "foreignKey": true,
      "defaultValue": null,
      "indexed": true,
      "referencedTable": "teams",
      "referencedColumn": "id"
    }
  ],
  "indexes": [
    {
      "indexName": "PRIMARY",
      "columnName": "id",
      "unique": true,
      "ordinalPosition": 1
    },
    {
      "indexName": "idx_matches_home_team_date",
      "columnName": "home_team_id",
      "unique": false,
      "ordinalPosition": 1
    }
  ]
}
```

### `GET /api/v1/database/relationships`
Returns all foreign key constraints and relations in the schema.

**Response `200 OK`:**
```json
[
  {
    "sourceTable": "historical_matches",
    "sourceColumn": "home_team_id",
    "referencedTable": "teams",
    "referencedColumn": "id",
    "constraintName": "fk_matches_home_team"
  },
  {
    "sourceTable": "match_source_records",
    "sourceColumn": "historical_match_id",
    "referencedTable": "historical_matches",
    "referencedColumn": "id",
    "constraintName": "fk_source_records_match"
  }
]
```

### `GET /api/v1/database/migrations`
Returns the Flyway versioned schema migration history.

**Response `200 OK`:**
```json
[
  {
    "version": "1",
    "description": "create competitions tables",
    "type": "SQL",
    "script": "V1__create_competitions_tables.sql",
    "checksum": 1782348231,
    "installedBy": "root",
    "installedOn": "2026-11-07T08:40:01.343Z",
    "executionTimeMs": 235,
    "success": true
  }
]
```
