package com.househost.config;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerCheckOutHistoryTest {

    @Test
    void addsHistoryEvidenceAndProtectsLegacyCompletedCheckouts() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, true);
        mockColumnExists(jdbcTemplate, "guest_history_applied", false);

        databaseSchemaCompatibilityRunner.ensureCheckOutGuestHistoryColumns();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table check_outs add column guest_history_applied bit not null default 0"
        );
        migrationOrder.verify(jdbcTemplate).execute("""
                update check_outs
                set guest_history_applied = 1
                where status = 'COMPLETED'
                  and guest_history_applied = 0
                """);
    }

    @Test
    void repeatedExecutionRunsNoDdlWhenColumnsAlreadyExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, true);
        mockColumnExists(jdbcTemplate, "guest_history_applied", true);

        databaseSchemaCompatibilityRunner.ensureCheckOutGuestHistoryColumns();
        databaseSchemaCompatibilityRunner.ensureCheckOutGuestHistoryColumns();

        verify(jdbcTemplate, never()).execute(
                "alter table check_outs add column guest_history_applied bit not null default 0"
        );
        verify(jdbcTemplate, times(2)).execute("""
                update check_outs
                set guest_history_applied = 1
                where status = 'COMPLETED'
                  and guest_history_applied = 0
                """);
    }

    @Test
    void skipsMigrationWhenCheckOutTableDoesNotExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, false);

        databaseSchemaCompatibilityRunner.ensureCheckOutGuestHistoryColumns();

        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void dropsObsoleteGenericRatingColumnsIdempotently() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("guests")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("check_outs")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("guests"),
                eq("rating")
        )).thenReturn(1, 0);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("check_outs"),
                eq("rating")
        )).thenReturn(1, 0);

        databaseSchemaCompatibilityRunner.removeObsoleteGenericRatingColumns();
        databaseSchemaCompatibilityRunner.removeObsoleteGenericRatingColumns();

        verify(jdbcTemplate).execute("alter table `guests` drop column `rating`");
        verify(jdbcTemplate).execute("alter table `check_outs` drop column `rating`");
    }

    private DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner(
            JdbcTemplate jdbcTemplate
    ) {
        return new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);
    }

    private void mockTableExists(JdbcTemplate jdbcTemplate, boolean exists) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("check_outs")
        )).thenReturn(exists ? 1 : 0);
    }

    private void mockColumnExists(
            JdbcTemplate jdbcTemplate,
            String columnName,
            boolean exists
    ) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("check_outs"),
                eq(columnName)
        )).thenReturn(exists ? 1 : 0);
    }
}
