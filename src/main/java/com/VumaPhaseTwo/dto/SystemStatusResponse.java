package com.VumaPhaseTwo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusResponse {
    private String applicationName;
    private String applicationVersion;
    private String environment;
    private String status;
    private String javaVersion;
    private String springBootVersion;
    private DatabaseStatus database;
    private Instant timestamp;
    private Map<String, Object> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseStatus {
        private String status;
        private String databaseName;
        private String databaseProductName;
        private String databaseProductVersion;
        private String connectionUrl;
        private int tableCount;
        private long pingTimeMs;
        private String error;
    }
}
