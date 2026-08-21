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

class DatabaseSchemaCompatibilityRunnerFinancialTransactionPlanTest {

    @Test
    void createsIdempotentCommandSchemaAndOutcomeIndex() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                anyString(),
                anyString()
        )).thenReturn(0);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);

        databaseSchemaCompatibilityRunner.ensureFinancialCommandIdempotencySchema();

        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create table if not exists financial_command_idempotency"
        ));
        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "constraint uk_financial_command_idempotency_scope unique"
        ));
        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "add column financial_transaction_id bigint null"
        ));
        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create index idx_financial_command_idempotency_outcome"
        ));
    }

    @Test
    void createsPlanSchemaMembershipOrderAndIndexesWithoutMigratingHistoricalPlans() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenAnswer(invocation -> {
                    String tableName = invocation.getArgument(2);
                    return "financial_transactions".equals(tableName) ? 1 : 0;
                });
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                anyString(),
                anyString()
        )).thenReturn(0);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);

        databaseSchemaCompatibilityRunner.ensureFinancialTransactionPlanSchema();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create table if not exists financial_transaction_plans"
        ));
        migrationOrder.verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create index idx_financial_transaction_plan_source"
        ));
        migrationOrder.verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "modify column source_type enum"
        ));
        migrationOrder.verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "add column plan_component_order int null"
        ));
        migrationOrder.verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create index idx_financial_transaction_plan_membership"
        ));
        verify(jdbcTemplate, never()).execute(org.mockito.ArgumentMatchers.contains(
                "update installment_plan_transactions"
        ));
    }

    @Test
    void preservesExistingPlanSchemaAndMembershipColumnOnRepeatedExecution() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                anyString(),
                anyString()
        )).thenReturn(1);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);

        databaseSchemaCompatibilityRunner.ensureFinancialTransactionPlanSchema();

        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "create table if not exists financial_transaction_plans"
        ));
        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains(
                "modify column source_type enum"
        ));
        verify(jdbcTemplate, never()).execute(org.mockito.ArgumentMatchers.contains(
                "add column plan_component_order"
        ));
        verify(jdbcTemplate, never()).execute(org.mockito.ArgumentMatchers.contains(
                "create index idx_financial_transaction_plan_source"
        ));
        verify(jdbcTemplate, never()).execute(org.mockito.ArgumentMatchers.contains(
                "create index idx_financial_transaction_plan_membership"
        ));
    }
}
