export interface DatabaseStatus {
  status: 'ONLINE' | 'OFFLINE' | 'DEGRADED';
  databaseName: string;
  databaseProductName?: string;
  databaseProductVersion?: string;
  connectionUrl?: string;
  tableCount: number;
  pingTimeMs: number;
  error?: string;
}

export interface SystemStatusResponse {
  applicationName: string;
  applicationVersion: string;
  environment: string;
  status: 'HEALTHY' | 'DEGRADED' | 'DOWN';
  javaVersion: string;
  springBootVersion: string;
  database: DatabaseStatus;
  timestamp: string;
  details: {
    activeProfile?: string;
    osName?: string;
    osArch?: string;
    availableProcessors?: number;
    freeMemoryMb?: number;
    totalMemoryMb?: number;
    [key: string]: any;
  };
}
