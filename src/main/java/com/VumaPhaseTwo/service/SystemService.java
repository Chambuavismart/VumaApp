package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.SystemStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

    private final DataSource dataSource;

    @Value("${spring.application.name:VumaPhaseTwo}")
    private String applicationName;

    @Value("${app.version:1.0.0-SNAPSHOT}")
    private String applicationVersion;

    public SystemStatusResponse getSystemStatus() {
        SystemStatusResponse.DatabaseStatus dbStatus = checkDatabaseHealth();
        
        Map<String, Object> details = new HashMap<>();
        details.put("activeProfile", System.getProperty("spring.profiles.active", "default"));
        details.put("osName", System.getProperty("os.name"));
        details.put("osArch", System.getProperty("os.arch"));
        details.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        details.put("freeMemoryMb", Runtime.getRuntime().freeMemory() / (1024 * 1024));
        details.put("totalMemoryMb", Runtime.getRuntime().totalMemory() / (1024 * 1024));

        return SystemStatusResponse.builder()
                .applicationName(applicationName)
                .applicationVersion(applicationVersion)
                .environment("Development")
                .status("ONLINE".equalsIgnoreCase(dbStatus.getStatus()) ? "HEALTHY" : "DEGRADED")
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion(SpringBootVersion.getVersion())
                .database(dbStatus)
                .timestamp(Instant.now())
                .details(details)
                .build();
    }

    private SystemStatusResponse.DatabaseStatus checkDatabaseHealth() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            long pingTime = System.currentTimeMillis() - start;
            DatabaseMetaData metaData = conn.getMetaData();
            
            String catalog = conn.getCatalog();
            int tableCount = 0;

            // Direct accurate query on MySQL information_schema
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                if (rs.next()) {
                    tableCount = rs.getInt(1);
                }
            } catch (Exception e) {
                // Fallback to JDBC metadata
                try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tableCount++;
                    }
                }
            }

            String url = metaData.getURL();
            // Sanitize URL for security (remove passwords / sensitive params)
            String sanitizedUrl = url != null ? url.replaceAll("(?i)password=[^&;]*", "password=***") : "Unknown";

            return SystemStatusResponse.DatabaseStatus.builder()
                    .status("ONLINE")
                    .databaseName(catalog != null ? catalog : "Vumaapp")
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .connectionUrl(sanitizedUrl)
                    .tableCount(tableCount)
                    .pingTimeMs(pingTime)
                    .build();
        } catch (Exception e) {
            log.error("Database connection inspection failed: {}", e.getMessage());
            return SystemStatusResponse.DatabaseStatus.builder()
                    .status("OFFLINE")
                    .databaseName("Vumaapp")
                    .error(e.getMessage())
                    .pingTimeMs(-1)
                    .build();
        }
    }
}
