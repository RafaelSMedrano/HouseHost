package com.househost.config;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerGuestSchemaTest {

    @Test
    void createsTextColumnsAndDropsObsoleteCareStorageWithoutCopyingLegacyData() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockTableExists(jdbcTemplate, "guest_preferences", true);
        mockColumnExists(jdbcTemplate, "guests", "preferences_and_restrictions", false);
        mockColumnExists(jdbcTemplate, "guests", "accessibility_needs", false);
        mockColumnExists(jdbcTemplate, "guests", "travels_with_pets", true);
        mockColumnExists(jdbcTemplate, "guests", "pet_type", true);
        mockColumnExists(jdbcTemplate, "guests", "favorite_room", true);
        mockColumnExists(jdbcTemplate, "guests", "needs_accessibility", true);

        databaseSchemaCompatibilityRunner.ensureGuestCareStorage();

        verify(jdbcTemplate).execute(
                "alter table guests add column `preferences_and_restrictions` text null"
        );
        verify(jdbcTemplate).execute(
                "alter table guests add column `accessibility_needs` text null"
        );
        verify(jdbcTemplate).execute("drop table guest_preferences");
        verify(jdbcTemplate).execute("alter table guests drop column `travels_with_pets`");
        verify(jdbcTemplate).execute("alter table guests drop column `pet_type`");
        verify(jdbcTemplate).execute("alter table guests drop column `favorite_room`");
        verify(jdbcTemplate).execute("alter table guests drop column `needs_accessibility`");

        ArgumentCaptor<String> executedSqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(7)).execute(executedSqlCaptor.capture());
        List<String> executedSqlList = executedSqlCaptor.getAllValues();
        assertFalse(executedSqlList.stream().anyMatch(
                executedSql -> executedSql.toLowerCase().startsWith("update ")
                        || executedSql.toLowerCase().startsWith("insert ")
        ));
    }

    @Test
    void dropsObsoleteGuestReferralColumnIdempotently() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockColumnExists(jdbcTemplate, "guests", "referred_by", true);

        databaseSchemaCompatibilityRunner.removeObsoleteGuestReferralColumn();

        verify(jdbcTemplate).execute("alter table guests drop column `referred_by`");
    }

    @Test
    void preservesSchemaWhenGuestReferralColumnIsAlreadyAbsent() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockColumnExists(jdbcTemplate, "guests", "referred_by", false);

        databaseSchemaCompatibilityRunner.removeObsoleteGuestReferralColumn();

        verify(jdbcTemplate, never()).execute("alter table guests drop column `referred_by`");
    }

    @Test
    void migratesLegacyStatusUsingBookingPriorityBeforeNarrowingTheEnum() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockColumnExists(jdbcTemplate, "guests", "status", true);
        mockBookingStatusSource(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(String.class),
                eq("guests"),
                eq("status")
        )).thenReturn("enum('IN_BOOKING','IN_STAY','GOT_CHECKOUT')|NO|IN_BOOKING");

        databaseSchemaCompatibilityRunner.ensureGuestStatusColumn();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute(legacyGuestStatusEnumSql());
        migrationOrder.verify(jdbcTemplate).execute(guestStatusSynchronizationSql());
        migrationOrder.verify(jdbcTemplate).execute(authoritativeGuestStatusEnumSql());
    }

    @Test
    void createsMissingStatusWithInactiveDefaultAndSynchronizesExistingRows() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockColumnExists(jdbcTemplate, "guests", "status", false);
        mockBookingStatusSource(jdbcTemplate);

        databaseSchemaCompatibilityRunner.ensureGuestStatusColumn();

        InOrder migrationOrder = inOrder(jdbcTemplate);
        migrationOrder.verify(jdbcTemplate).execute(addAuthoritativeGuestStatusSql());
        migrationOrder.verify(jdbcTemplate).execute(guestStatusSynchronizationSql());
    }

    @Test
    void repeatedExecutionPreservesCompatibleColumnsAndRunsNoDestructiveDdl() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockTableExists(jdbcTemplate, "guest_preferences", false);
        mockColumnExists(jdbcTemplate, "guests", "preferences_and_restrictions", true);
        mockColumnExists(jdbcTemplate, "guests", "accessibility_needs", true);
        mockColumnExists(jdbcTemplate, "guests", "travels_with_pets", false);
        mockColumnExists(jdbcTemplate, "guests", "pet_type", false);
        mockColumnExists(jdbcTemplate, "guests", "favorite_room", false);
        mockColumnExists(jdbcTemplate, "guests", "needs_accessibility", false);
        mockColumnExists(jdbcTemplate, "guests", "status", true);
        mockBookingStatusSource(jdbcTemplate);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(String.class),
                eq("guests"),
                eq("status")
        )).thenReturn(
                "enum('WITH_UNCONFIRMED_BOOKING','WITH_CONFIRMED_BOOKING',"
                        + "'IN_STAY','INACTIVE')|NO|INACTIVE"
        );

        databaseSchemaCompatibilityRunner.ensureGuestCareStorage();
        databaseSchemaCompatibilityRunner.ensureGuestStatusColumn();
        databaseSchemaCompatibilityRunner.ensureGuestCareStorage();
        databaseSchemaCompatibilityRunner.ensureGuestStatusColumn();

        verify(jdbcTemplate, never()).execute(argThat(
                (String executedSql) -> executedSql != null
                        && (executedSql.startsWith("alter table guests")
                        || executedSql.startsWith("drop table guest_preferences"))
        ));
        verify(jdbcTemplate, times(2)).execute(guestStatusSynchronizationSql());
    }

    @Test
    void fallsBackToInactiveWhenBookingStatusSourceIsUnavailable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner =
                databaseSchemaCompatibilityRunner(jdbcTemplate);
        mockTableExists(jdbcTemplate, "guests", true);
        mockColumnExists(jdbcTemplate, "guests", "status", true);
        mockTableExists(jdbcTemplate, "bookings", false);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(String.class),
                eq("guests"),
                eq("status")
        )).thenReturn(
                "enum('WITH_UNCONFIRMED_BOOKING','WITH_CONFIRMED_BOOKING',"
                        + "'IN_STAY','INACTIVE')|NO|INACTIVE"
        );

        databaseSchemaCompatibilityRunner.ensureGuestStatusColumn();

        verify(jdbcTemplate).execute("update guests set status = 'INACTIVE'");
        verify(jdbcTemplate, never()).execute(argThat(
                (String executedSql) -> executedSql != null
                        && executedSql.startsWith("alter table guests")
        ));
    }

    private DatabaseSchemaCompatibilityRunner databaseSchemaCompatibilityRunner(
            JdbcTemplate jdbcTemplate
    ) {
        return new DatabaseSchemaCompatibilityRunner(
                mock(DataSource.class),
                jdbcTemplate
        );
    }

    private void mockBookingStatusSource(JdbcTemplate jdbcTemplate) {
        mockTableExists(jdbcTemplate, "bookings", true);
        mockColumnExists(jdbcTemplate, "bookings", "guest_id", true);
        mockColumnExists(jdbcTemplate, "bookings", "status", true);
    }

    private void mockTableExists(
            JdbcTemplate jdbcTemplate,
            String tableName,
            boolean exists
    ) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(tableName)
        )).thenReturn(exists ? 1 : 0);
    }

    private void mockColumnExists(
            JdbcTemplate jdbcTemplate,
            String tableName,
            String columnName,
            boolean exists
    ) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq(tableName),
                eq(columnName)
        )).thenReturn(exists ? 1 : 0);
    }

    private String addAuthoritativeGuestStatusSql() {
        return """
                alter table guests
                add column status enum(
                    'WITH_UNCONFIRMED_BOOKING',
                    'WITH_CONFIRMED_BOOKING',
                    'IN_STAY',
                    'INACTIVE'
                ) not null default 'INACTIVE'
                """;
    }

    private String legacyGuestStatusEnumSql() {
        return """
                alter table guests
                modify column status enum(
                    'COM_RESERVA',
                    'EM_ESTADIA',
                    'COM_CHECK_OUT',
                    'IN_BOOKING',
                    'GOT_CHECKOUT',
                    'WITH_UNCONFIRMED_BOOKING',
                    'WITH_CONFIRMED_BOOKING',
                    'IN_STAY',
                    'INACTIVE'
                ) not null default 'INACTIVE'
                """;
    }

    private String authoritativeGuestStatusEnumSql() {
        return """
                alter table guests
                modify column status enum(
                    'WITH_UNCONFIRMED_BOOKING',
                    'WITH_CONFIRMED_BOOKING',
                    'IN_STAY',
                    'INACTIVE'
                ) not null default 'INACTIVE'
                """;
    }

    private String guestStatusSynchronizationSql() {
        return """
                update guests guest
                set guest.status = case
                    when exists (
                        select 1
                        from bookings booking
                        where booking.guest_id = guest.id
                          and booking.status = 'IN_STAY'
                    ) then 'IN_STAY'
                    when exists (
                        select 1
                        from bookings booking
                        where booking.guest_id = guest.id
                          and booking.status = 'CONFIRMED'
                    ) then 'WITH_CONFIRMED_BOOKING'
                    when exists (
                        select 1
                        from bookings booking
                        where booking.guest_id = guest.id
                          and booking.status = 'UNCONFIRMED'
                    ) then 'WITH_UNCONFIRMED_BOOKING'
                    else 'INACTIVE'
                end
                """;
    }
}
