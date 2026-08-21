package com.househost.config;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseSchemaCompatibilityRunnerStayHistoryTest {

    @Test
    void makesBothBookingLinksNullableAndReplacesForeignKeysWithSetNull() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner runner = runner(jdbcTemplate);
        mockSchemaObjectExistence(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("check_ins"), eq("booking_id")))
                .thenReturn(List.of("old_check_in_booking_fk"));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("check_outs"), eq("booking_id")))
                .thenReturn(List.of("old_check_out_booking_fk"));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("check_ins"), eq("booking_id")))
                .thenReturn("NO");
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("check_outs"), eq("booking_id")))
                .thenReturn("NO");

        runner.ensureStayHistoryBookingLinksAreOptional();

        verify(jdbcTemplate).execute(
                "alter table `check_ins` drop foreign key `old_check_in_booking_fk`");
        verify(jdbcTemplate).execute("alter table `check_ins` modify column booking_id bigint null");
        verify(jdbcTemplate).execute("alter table `check_ins` add constraint `fk_check_ins_booking` "
                + "foreign key (booking_id) references bookings(id) on delete set null");
        verify(jdbcTemplate).execute(
                "alter table `check_outs` drop foreign key `old_check_out_booking_fk`");
        verify(jdbcTemplate).execute("alter table `check_outs` modify column booking_id bigint null");
        verify(jdbcTemplate).execute("alter table `check_outs` add constraint `fk_check_outs_booking` "
                + "foreign key (booking_id) references bookings(id) on delete set null");
    }

    @Test
    void doesNotRunDdlWhenSchemaAlreadySupportsHistoryDetachment() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseSchemaCompatibilityRunner runner = runner(jdbcTemplate);
        mockSchemaObjectExistence(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("check_ins"), eq("booking_id")))
                .thenReturn(List.of("fk_check_ins_booking"));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), eq("check_outs"), eq("booking_id")))
                .thenReturn(List.of("fk_check_outs_booking"));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("check_ins"), eq("booking_id")))
                .thenReturn("YES");
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq("check_outs"), eq("booking_id")))
                .thenReturn("YES");
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("check_ins"), eq("booking_id")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("check_outs"), eq("booking_id")))
                .thenReturn(1);

        runner.ensureStayHistoryBookingLinksAreOptional();

        verify(jdbcTemplate, never()).execute(anyString());
    }

    private DatabaseSchemaCompatibilityRunner runner(JdbcTemplate jdbcTemplate) {
        return new DatabaseSchemaCompatibilityRunner(mock(DataSource.class), jdbcTemplate);
    }

    private void mockSchemaObjectExistence(JdbcTemplate jdbcTemplate) {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("bookings")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("check_ins")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("check_outs")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class), eq("check_ins"), eq("booking_id")))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(
                anyString(), eq(Integer.class), eq("check_outs"), eq("booking_id")))
                .thenReturn(1);
    }
}
