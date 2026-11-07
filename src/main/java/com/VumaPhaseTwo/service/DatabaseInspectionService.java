package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.DatabaseDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseInspectionService {

    private final DataSource dataSource;
    private final Flyway flyway;

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public OverviewResponse getDatabaseOverview() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbName = conn.getCatalog();
            String engine = metaData.getDatabaseProductName();
            String version = metaData.getDatabaseProductVersion();

            int totalTables = 0;
            int totalColumns = 0;
            int totalForeignKeys = 0;
            int totalIndexes = 0;
            String charset = "utf8mb4";
            String collation = "utf8mb4_unicode_ci";

            try (Statement stmt = conn.createStatement()) {
                // Table count
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                    if (rs.next()) totalTables = rs.getInt(1);
                }

                // Column count
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE()")) {
                    if (rs.next()) totalColumns = rs.getInt(1);
                }

                // FK count
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND constraint_type = 'FOREIGN KEY'")) {
                    if (rs.next()) totalForeignKeys = rs.getInt(1);
                }

                // Index count
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(DISTINCT table_name, index_name) FROM information_schema.statistics WHERE table_schema = DATABASE()")) {
                    if (rs.next()) totalIndexes = rs.getInt(1);
                }

                // Charset & Collation
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT default_character_set_name, default_collation_name FROM information_schema.schemata WHERE schema_name = DATABASE()")) {
                    if (rs.next()) {
                        charset = rs.getString(1);
                        collation = rs.getString(2);
                    }
                }
            }

            // Flyway Status
            String flywayStatus = "SUCCESS";
            String latestMigration = "None";
            try {
                MigrationInfo[] migrationInfos = flyway.info().all();
                if (migrationInfos != null && migrationInfos.length > 0) {
                    MigrationInfo current = flyway.info().current();
                    if (current != null) {
                        latestMigration = current.getVersion() != null ?
                                current.getVersion().getVersion() + " - " + current.getDescription() : current.getDescription();
                        flywayStatus = current.getState().name();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch Flyway info: {}", e.getMessage());
                flywayStatus = "UNKNOWN";
            }

            return OverviewResponse.builder()
                    .databaseName(dbName != null ? dbName : "vumaapp")
                    .databaseEngine(engine)
                    .databaseVersion(version)
                    .connectionStatus("ONLINE")
                    .totalTables(totalTables)
                    .totalColumns(totalColumns)
                    .totalForeignKeys(totalForeignKeys)
                    .totalIndexes(totalIndexes)
                    .flywayStatus(flywayStatus)
                    .latestAppliedMigration(latestMigration)
                    .characterSet(charset)
                    .collation(collation)
                    .build();

        } catch (Exception e) {
            log.error("Failed to inspect database overview", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database inspection failed: " + e.getMessage());
        }
    }

    public List<TableSummaryResponse> getTableSummaries() {
        List<TableSummaryResponse> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT t.table_name, t.table_type, t.table_rows,
                       (SELECT COUNT(*) FROM information_schema.columns c WHERE c.table_schema = DATABASE() AND c.table_name = t.table_name) as col_count,
                       (SELECT COUNT(DISTINCT s.index_name) FROM information_schema.statistics s WHERE s.table_schema = DATABASE() AND s.table_name = t.table_name) as idx_count,
                       (SELECT COUNT(*) FROM information_schema.table_constraints tc WHERE tc.table_schema = DATABASE() AND tc.table_name = t.table_name AND tc.constraint_type = 'FOREIGN KEY') as fk_count
                FROM information_schema.tables t
                WHERE t.table_schema = DATABASE()
                ORDER BY t.table_name ASC
            """;

            try (ResultSet rs = stmt.executeQuery(query)) {
                int index = 1;
                while (rs.next()) {
                    tables.add(TableSummaryResponse.builder()
                            .number(index++)
                            .tableName(rs.getString("table_name"))
                            .tableType(rs.getString("table_type"))
                            .approximateRowCount(rs.getLong("table_rows"))
                            .columnCount(rs.getInt("col_count"))
                            .indexCount(rs.getInt("idx_count"))
                            .foreignKeyCount(rs.getInt("fk_count"))
                            .build());
                }
            }
            return tables;
        } catch (Exception e) {
            log.error("Failed to inspect table list", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Table listing inspection failed: " + e.getMessage());
        }
    }

    public TableStructureResponse getTableStructure(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid table name format");
        }

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            // Verify table existence in this schema
            boolean exists = false;
            try (ResultSet rs = metaData.getTables(catalog, null, tableName, new String[]{"TABLE", "VIEW", "SYSTEM TABLE"})) {
                if (rs.next()) exists = true;
            }
            if (!exists) {
                // Case fallback check
                try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE", "VIEW", "SYSTEM TABLE"})) {
                    while (rs.next()) {
                        if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                            tableName = rs.getString("TABLE_NAME");
                            exists = true;
                            break;
                        }
                    }
                }
            }

            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Table '" + tableName + "' not found in database");
            }

            // Primary keys
            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet rs = metaData.getPrimaryKeys(catalog, null, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }

            // Foreign keys
            Map<String, String[]> foreignKeys = new HashMap<>(); // col -> [refTable, refCol]
            try (ResultSet rs = metaData.getImportedKeys(catalog, null, tableName)) {
                while (rs.next()) {
                    String fkCol = rs.getString("FKCOLUMN_NAME");
                    String pkTable = rs.getString("PKTABLE_NAME");
                    String pkCol = rs.getString("PKCOLUMN_NAME");
                    foreignKeys.put(fkCol, new String[]{pkTable, pkCol});
                }
            }

            // Indexes
            List<IndexDetailResponse> indexes = new ArrayList<>();
            Set<String> indexedCols = new HashSet<>();
            try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableName, false, true)) {
                while (rs.next()) {
                    String idxName = rs.getString("INDEX_NAME");
                    String colName = rs.getString("COLUMN_NAME");
                    if (idxName != null && colName != null) {
                        indexedCols.add(colName);
                        indexes.add(IndexDetailResponse.builder()
                                .indexName(idxName)
                                .columnName(colName)
                                .unique(!rs.getBoolean("NON_UNIQUE"))
                                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                                .build());
                    }
                }
            }

            // Columns
            List<ColumnDetailResponse> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(catalog, null, tableName, "%")) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String typeName = rs.getString("TYPE_NAME");
                    int colSize = rs.getInt("COLUMN_SIZE");
                    int nullableInt = rs.getInt("NULLABLE");
                    String defVal = rs.getString("COLUMN_DEF");
                    int pos = rs.getInt("ORDINAL_POSITION");

                    boolean isPk = primaryKeys.contains(colName);
                    boolean isFk = foreignKeys.containsKey(colName);
                    String refTable = isFk ? foreignKeys.get(colName)[0] : null;
                    String refCol = isFk ? foreignKeys.get(colName)[1] : null;

                    columns.add(ColumnDetailResponse.builder()
                            .position(pos)
                            .columnName(colName)
                            .dataType(typeName + (colSize > 0 && !typeName.equalsIgnoreCase("TEXT") && !typeName.equalsIgnoreCase("INT") && !typeName.equalsIgnoreCase("BIGINT") && !typeName.equalsIgnoreCase("DATE") && !typeName.equalsIgnoreCase("TIME") && !typeName.equalsIgnoreCase("TIMESTAMP") ? "(" + colSize + ")" : ""))
                            .nullable(nullableInt == DatabaseMetaData.columnNullable)
                            .primaryKey(isPk)
                            .foreignKey(isFk)
                            .defaultValue(defVal)
                            .indexed(indexedCols.contains(colName))
                            .referencedTable(refTable)
                            .referencedColumn(refCol)
                            .build());
                }
            }

            return TableStructureResponse.builder()
                    .tableName(tableName)
                    .columns(columns)
                    .indexes(indexes)
                    .build();

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            log.error("Failed to inspect structure for table " + tableName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to inspect table structure: " + e.getMessage());
        }
    }

    public List<RelationshipResponse> getRelationships() {
        List<RelationshipResponse> relationships = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT 
                    TABLE_NAME AS source_table,
                    COLUMN_NAME AS source_column,
                    REFERENCED_TABLE_NAME AS referenced_table,
                    REFERENCED_COLUMN_NAME AS referenced_column,
                    CONSTRAINT_NAME AS constraint_name
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                ORDER BY TABLE_NAME, CONSTRAINT_NAME, ORDINAL_POSITION
            """;

            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    relationships.add(RelationshipResponse.builder()
                            .sourceTable(rs.getString("source_table"))
                            .sourceColumn(rs.getString("source_column"))
                            .referencedTable(rs.getString("referenced_table"))
                            .referencedColumn(rs.getString("referenced_column"))
                            .constraintName(rs.getString("constraint_name"))
                            .build());
                }
            }
            return relationships;
        } catch (Exception e) {
            log.error("Failed to inspect relationships", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to inspect relationships: " + e.getMessage());
        }
    }

    public List<MigrationRecordResponse> getMigrationHistory() {
        List<MigrationRecordResponse> records = new ArrayList<>();
        try {
            MigrationInfo[] migrationInfos = flyway.info().all();
            if (migrationInfos != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                for (MigrationInfo info : migrationInfos) {
                    records.add(MigrationRecordResponse.builder()
                            .version(info.getVersion() != null ? info.getVersion().getVersion() : "N/A")
                            .description(info.getDescription())
                            .type(info.getType() != null ? info.getType().name() : "SQL")
                            .script(info.getScript())
                            .checksum(info.getChecksum())
                            .installedBy(info.getInstalledBy())
                            .installedOn(info.getInstalledOn() != null ? info.getInstalledOn().toInstant().toString() : null)
                            .executionTimeMs(info.getExecutionTime())
                            .success(info.getState() != null && info.getState().isApplied())
                            .build());
                }
            }
            return records;
        } catch (Exception e) {
            log.error("Failed to retrieve migration history", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve migration history: " + e.getMessage());
        }
    }
}
