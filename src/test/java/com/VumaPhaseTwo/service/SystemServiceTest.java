package com.VumaPhaseTwo.service;

import com.VumaPhaseTwo.dto.SystemStatusResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private SystemService systemService;

    @Test
    @DisplayName("getSystemStatus returns HEALTHY when database is connected")
    void testGetSystemStatusOnline() throws SQLException {
        ReflectionTestUtils.setField(systemService, "applicationName", "VumaPhaseTwo");
        ReflectionTestUtils.setField(systemService, "applicationVersion", "1.0.0-SNAPSHOT");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("Vumaapp");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);

        when(databaseMetaData.getDatabaseProductName()).thenReturn("MySQL");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("8.0.46");
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/Vumaapp");

        SystemStatusResponse status = systemService.getSystemStatus();

        assertThat(status).isNotNull();
        assertThat(status.getApplicationName()).isEqualTo("VumaPhaseTwo");
        assertThat(status.getStatus()).isEqualTo("HEALTHY");
        assertThat(status.getDatabase().getStatus()).isEqualTo("ONLINE");
        assertThat(status.getDatabase().getDatabaseName()).isEqualTo("Vumaapp");
        assertThat(status.getDatabase().getDatabaseProductName()).isEqualTo("MySQL");
        assertThat(status.getDatabase().getTableCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getSystemStatus returns DEGRADED when database is offline")
    void testGetSystemStatusOffline() throws SQLException {
        ReflectionTestUtils.setField(systemService, "applicationName", "VumaPhaseTwo");
        ReflectionTestUtils.setField(systemService, "applicationVersion", "1.0.0-SNAPSHOT");

        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        SystemStatusResponse status = systemService.getSystemStatus();

        assertThat(status).isNotNull();
        assertThat(status.getStatus()).isEqualTo("DEGRADED");
        assertThat(status.getDatabase().getStatus()).isEqualTo("OFFLINE");
        assertThat(status.getDatabase().getError()).contains("Connection refused");
    }
}
