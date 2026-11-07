export interface DatabaseOverview {
  databaseName: string;
  databaseEngine: string;
  databaseVersion: string;
  connectionStatus: string;
  totalTables: number;
  totalColumns: number;
  totalForeignKeys: number;
  totalIndexes: number;
  flywayStatus: string;
  latestAppliedMigration: string;
  characterSet: string;
  collation: string;
}

export interface TableSummary {
  number: number;
  tableName: string;
  tableType: string;
  approximateRowCount: number;
  columnCount: number;
  indexCount: number;
  foreignKeyCount: number;
}

export interface ColumnDetail {
  position: number;
  columnName: string;
  dataType: string;
  nullable: boolean;
  primaryKey: boolean;
  foreignKey: boolean;
  defaultValue: string | null;
  indexed: boolean;
  referencedTable: string | null;
  referencedColumn: string | null;
}

export interface IndexDetail {
  indexName: string;
  columnName: string;
  unique: boolean;
  ordinalPosition: number;
}

export interface TableStructure {
  tableName: string;
  columns: ColumnDetail[];
  indexes: IndexDetail[];
}

export interface RelationshipDetail {
  sourceTable: string;
  sourceColumn: string;
  referencedTable: string;
  referencedColumn: string;
  constraintName: string;
}

export interface MigrationRecord {
  version: string;
  description: string;
  type: string;
  script: string;
  checksum: number;
  installedBy: string;
  installedOn: string;
  executionTimeMs: number;
  success: boolean;
}
