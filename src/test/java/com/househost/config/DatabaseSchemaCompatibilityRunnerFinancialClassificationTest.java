package com.househost.config;

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

class DatabaseSchemaCompatibilityRunnerFinancialClassificationTest {

    @Test
    void removesDirectionalAmountsAndMigratesLegacyTypesToAuthoritativeTaxonomy() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                anyString()
        )).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("financial_transactions"),
                anyString()
        )).thenReturn(1);

        databaseSchemaCompatibilityRunner.migrateFinancialTransactionClassification();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set amount = abs(amount)
                """);
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `financial_transactions` drop column `entry_amount`"
        );
        migrationOrder.verify(jdbcTemplate).execute(
                "alter table `financial_transactions` drop column `expense_amount`"
        );
        migrationOrder.verify(jdbcTemplate).execute("""
                alter table financial_transactions
                modify column type varchar(50) null
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set type = 'PLAN_DOWN_PAYMENT'
                where type = 'PLAN_SIGNAL_TRANSACTIONAL'
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set type = 'PLAN_TRANSACTION'
                where type = 'PLAN_TRANSACTIONAL'
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set type = 'INSTALLMENT_PLAN_BLOCK'
                where id in (
                    select financial_transaction_id
                    from installment_plan_transactions
                )
                  and (
                      type is null
                      or type not in (
                          'PLAN_DOWN_PAYMENT',
                          'PLAN_CHECK_IN_PAYMENT',
                          'PLAN_CHECK_OUT_PAYMENT',
                          'PLAN_TRANSACTION',
                          'INSTALLMENT_PLAN_BLOCK'
                      )
                  )
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set type = 'INSTALLMENT_TRANSACTION'
                where id in (
                    select financial_transaction_id
                    from installment_transactions
                )
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set source_type = 'INSTALLMENT',
                    source_id = (
                        select installment_plan_id
                        from installment_transactions
                        where financial_transaction_id = financial_transactions.id
                    )
                where id in (
                    select financial_transaction_id
                    from installment_transactions
                )
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                update financial_transactions
                set type = 'STANDARD'
                where type is null
                   or type not in (
                       'STANDARD',
                       'PLAN_DOWN_PAYMENT',
                       'PLAN_CHECK_IN_PAYMENT',
                       'PLAN_CHECK_OUT_PAYMENT',
                       'PLAN_TRANSACTION',
                       'INSTALLMENT_PLAN_BLOCK',
                       'INSTALLMENT_TRANSACTION'
                   )
                """);
        migrationOrder.verify(jdbcTemplate).execute("""
                alter table financial_transactions
                modify column type varchar(50) not null default 'STANDARD'
                """);
    }

    @Test
    void doesNothingBeforeTheFinancialTransactionTableExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("financial_transactions")
        )).thenReturn(0);

        databaseSchemaCompatibilityRunner.migrateFinancialTransactionClassification();

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
