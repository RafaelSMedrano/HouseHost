package com.househost.config;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerFinancialBookingTest {

    @Test
    void migratesSourceBeforeRemovingEveryLegacyBookingConstraintAndColumn() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("financial_transactions"),
                eq("booking_id")
        )).thenReturn(1);
        when(jdbcTemplate.queryForList(
                anyString(),
                eq(String.class),
                eq("financial_transactions"),
                eq("booking_id")
        )).thenReturn(List.of("legacy_booking_fk", "secondary_booking_fk"));

        databaseSchemaCompatibilityRunner.removeLegacyFinancialTransactionBookingLink();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set source_type = 'BOOKING',
                    source_id = booking_id
                where source_type is null
                  and source_id is null
                  and booking_id is not null
                """);
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `financial_transactions` drop foreign key `legacy_booking_fk`"
        );
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `financial_transactions` drop foreign key `secondary_booking_fk`"
        );
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `financial_transactions` drop column booking_id"
        );
    }

    @Test
    void doesNothingWhenLegacyBookingColumnIsAlreadyAbsent() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("financial_transactions"),
                eq("booking_id")
        )).thenReturn(0);

        databaseSchemaCompatibilityRunner.removeLegacyFinancialTransactionBookingLink();

        verify(jdbcTemplate, never()).queryForList(
                anyString(),
                eq(String.class),
                anyString(),
                anyString()
        );
        verify(jdbcTemplate, never()).execute(anyString());
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
