package com.VumaPhaseTwo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DatabaseDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewResponse {
        private String databaseName;
        private String databaseEngine;
        private String databaseVersion;
        private String connectionStatus;
        private int totalTables;
        private int totalColumns;
        private int totalForeignKeys;
        private int totalIndexes;
        private String flywayStatus;
        private String latestAppliedMigration;
        private String characterSet;
        private String collation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableSummaryResponse {
        private int number;
        private String tableName;
        private String tableType;
        private long approximateRowCount;
        private int columnCount;
        private int indexCount;
        private int foreignKeyCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnDetailResponse {
        private int position;
        private String columnName;
        private String dataType;
        private boolean nullable;
        private boolean primaryKey;
        private boolean foreignKey;
        private String defaultValue;
        private boolean indexed;
        private String referencedTable;
        private String referencedColumn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexDetailResponse {
        private String indexName;
        private String columnName;
        private boolean unique;
        private int ordinalPosition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableStructureResponse {
        private String tableName;
        private List<ColumnDetailResponse> columns;
        private List<IndexDetailResponse> indexes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipResponse {
        private String sourceTable;
        private String sourceColumn;
        private String referencedTable;
        private String referencedColumn;
        private String constraintName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MigrationRecordResponse {
        private String version;
        private String description;
        private String type;
        private String script;
        private Integer checksum;
        private String installedBy;
        private String installedOn;
        private Integer executionTimeMs;
        private boolean success;
    }
}
