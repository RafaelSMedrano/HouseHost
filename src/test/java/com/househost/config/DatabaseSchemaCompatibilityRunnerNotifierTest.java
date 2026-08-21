package com.househost.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DatabaseSchemaCompatibilityRunnerNotifierTest {

    @Test
    void createsDecoupledNotifierTablesWithInternalConstraintsAndIndexes() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);

        databaseSchemaCompatibilityRunner.ensureNotifierSchema();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(11)).execute(sqlCaptor.capture());
        List<String> schemaStatementList = sqlCaptor.getAllValues();
        String intentSchema = schemaStatementList.getFirst();
        String eventSchema = schemaStatementList.get(1);
        String completeSchema = String.join("\n", schemaStatementList);
        assertTrue(intentSchema.contains("create table if not exists notification_intents"));
        assertTrue(intentSchema.contains("version bigint not null default 0"));
        assertTrue(intentSchema.contains("uk_notification_intent_source_idempotency"));
        assertTrue(intentSchema.contains("uk_notification_intent_provider_message"));
        assertTrue(eventSchema.contains(
                "create table if not exists notification_provider_events"
        ));
        assertTrue(eventSchema.contains("references notification_intents(id)"));
        assertTrue(completeSchema.contains("idx_notification_intent_dispatch"));
        assertTrue(completeSchema.contains("idx_notification_intent_retention"));
        assertTrue(completeSchema.contains("idx_notification_provider_event_intent"));
        assertTrue(completeSchema.contains("idx_notification_provider_event_message"));
        assertFalse(intentSchema.contains("booking_id"));
        assertFalse(eventSchema.contains("booking_id"));
        assertFalse(eventSchema.contains("raw_payload"));
    }
}
