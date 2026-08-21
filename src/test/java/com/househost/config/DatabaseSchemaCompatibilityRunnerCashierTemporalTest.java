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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerCashierTemporalTest {

    @Test
    void mapsLegacyMovementDatesToDueDatesWithoutInventingSettlementDates() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String columnName = invocation.getArgument(3);
                    return columnName.equals("entry_date") || columnName.equals("expense_date") ? 1 : 0;
                });

        databaseSchemaCompatibilityRunner.ensureCashierMovementTemporalColumns();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        verifyMovementMigration(jdbcTemplate, migrationOrder, "cashier_entries", "entry_date");
        verifyMovementMigration(jdbcTemplate, migrationOrder, "cashier_expenses", "expense_date");
    }

    @Test
    void doesNothingWhenCashierMovementTablesDoNotExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(0);

        databaseSchemaCompatibilityRunner.ensureCashierMovementTemporalColumns();

        verify(jdbcTemplate, never()).execute(anyString());
    }

    private void verifyMovementMigration(
            JdbcTemplate jdbcTemplate,
            InOrder migrationOrder,
            String tableName,
            String legacyDateColumnName
    ) {
        migrationOrder.verify(jdbcTemplate).execute(String.format("""
                alter table %s
                add column due_date date null
                """, tableName));
        migrationOrder.verify(jdbcTemplate).execute(String.format("""
                update %s
                set due_date = %s
                where due_date is null
                """, tableName, legacyDateColumnName));
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `" + tableName + "` drop column `" + legacyDateColumnName + "`"
        );
        migrationOrder.verify(jdbcTemplate).execute(String.format("""
                alter table %s
                add column settlement_date date null
                """, tableName));
        migrationOrder.verify(jdbcTemplate).execute(String.format("""
                alter table %s
                modify column due_date date not null
                """, tableName));
    }

    private DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner(
            JdbcTemplate jdbcTemplate
    ) {
        return new DatabaseSchemaCompatibilityRunner(
                mock(DataSource.class),
                jdbcTemplate
        );
    }
}
