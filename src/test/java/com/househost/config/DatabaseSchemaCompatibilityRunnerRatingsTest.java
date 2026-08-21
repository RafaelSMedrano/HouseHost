package com.househost.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerRatingsTest {

    @Test
    void createsIdempotentRatingsTableWithBookingAndScoreConstraints() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockBookingTableExists(jdbcTemplate, true);

        databaseSchemaCompatibilityRunner.ensureRatingsTable();
        databaseSchemaCompatibilityRunner.ensureRatingsTable();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(2)).execute(sqlCaptor.capture());
        String createTableSql = sqlCaptor.getValue();
        assertTrue(createTableSql.contains("create table if not exists ratings"));
        assertTrue(createTableSql.contains("booking_id bigint not null"));
        assertTrue(createTableSql.contains("constraint uk_ratings_booking unique (booking_id)"));
        assertTrue(createTableSql.contains("foreign key (booking_id) references bookings(id)"));
        assertTrue(createTableSql.contains("check_in_procedure_score between 1 and 5"));
        assertTrue(createTableSql.contains("check_out_procedure_score between 1 and 5"));
        assertTrue(createTableSql.contains("accommodation_cleanliness_score between 1 and 5"));
        assertTrue(createTableSql.contains("team_communication_score between 1 and 5"));
        assertTrue(createTableSql.contains("location_score between 1 and 5"));
        assertTrue(createTableSql.contains("comfort_score between 1 and 5"));
    }

    @Test
    void skipsRatingsTableUntilBookingsExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockBookingTableExists(jdbcTemplate, false);

        databaseSchemaCompatibilityRunner.ensureRatingsTable();

        verify(jdbcTemplate, never()).execute(anyString());
    }

    private DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner(
            JdbcTemplate jdbcTemplate
    ) {
        return new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);
    }

    private void mockBookingTableExists(JdbcTemplate jdbcTemplate, boolean exists) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("bookings")
        )).thenReturn(exists ? 1 : 0);
    }
}
