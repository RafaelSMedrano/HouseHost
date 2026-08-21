package com.househost.booking.booking.adapter.out.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingStayHistoryDeletionIntegrationTest {

    @Test
    void deletingBookingDetachesAndPreservesCheckInAndCheckOutHistory() {
        JdbcTemplate jdbcTemplate = fixtureDatabase();

        jdbcTemplate.update("delete from bookings where id = ?", 23L);

        assertEquals(0, count(jdbcTemplate, "bookings"));
        assertPreservedCheckIn(jdbcTemplate);
        assertPreservedCheckOut(jdbcTemplate);
    }

    @Test
    void rollingBackBookingDeletionRestoresBookingAndBothAssociations() throws SQLException {
        JdbcTemplate jdbcTemplate = fixtureDatabase();
        DataSource dataSource = jdbcTemplate.getDataSource();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.prepareStatement("delete from bookings where id = 23").executeUpdate();
            connection.rollback();
        }

        assertEquals(1, count(jdbcTemplate, "bookings"));
        assertEquals(23L, bookingId(jdbcTemplate, "check_ins"));
        assertEquals(23L, bookingId(jdbcTemplate, "check_outs"));
    }

    private JdbcTemplate fixtureDatabase() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:stay_history_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("create table guests (id bigint primary key, full_name varchar(120) not null)");
        jdbcTemplate.execute("create table rooms (id bigint primary key, room_number varchar(30) not null)");
        jdbcTemplate.execute("create table bookings (id bigint primary key, guest_id bigint not null, room_id bigint not null)");
        jdbcTemplate.execute("""
                create table check_ins (
                    id bigint primary key,
                    booking_id bigint unique null,
                    guest_id bigint not null,
                    room_id bigint not null,
                    adults integer,
                    status varchar(30) not null,
                    notes varchar(1000),
                    created_at timestamp not null,
                    constraint fk_check_ins_booking foreign key (booking_id)
                        references bookings(id) on delete set null
                )
                """);
        jdbcTemplate.execute("""
                create table check_outs (
                    id bigint primary key,
                    booking_id bigint unique null,
                    guest_id bigint not null,
                    room_id bigint not null,
                    keys_returned boolean not null,
                    status varchar(30) not null,
                    notes varchar(1000),
                    created_at timestamp not null,
                    constraint fk_check_outs_booking foreign key (booking_id)
                        references bookings(id) on delete set null
                )
                """);
        jdbcTemplate.update("insert into guests (id, full_name) values (?, ?)", 7L, "Roberto Jr");
        jdbcTemplate.update("insert into rooms (id, room_number) values (?, ?)", 1L, "101");
        jdbcTemplate.update("insert into bookings (id, guest_id, room_id) values (?, ?, ?)", 23L, 7L, 1L);
        jdbcTemplate.update("""
                insert into check_ins (
                    id, booking_id, guest_id, room_id, adults, status, notes, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, timestamp '2026-08-08 14:00:00')
                """, 11L, 23L, 7L, 1L, 2, "COMPLETED", "check-in preservado");
        jdbcTemplate.update("""
                insert into check_outs (
                    id, booking_id, guest_id, room_id, keys_returned, status, notes, created_at
                ) values (?, ?, ?, ?, ?, ?, ?, timestamp '2026-08-10 11:00:00')
                """, 12L, 23L, 7L, 1L, true, "COMPLETED", "checkout preservado");
        return jdbcTemplate;
    }

    private void assertPreservedCheckIn(JdbcTemplate jdbcTemplate) {
        Map<String, Object> row = jdbcTemplate.queryForMap("select * from check_ins where id = 11");

        assertNull(row.get("BOOKING_ID"));
        assertEquals(7L, row.get("GUEST_ID"));
        assertEquals(1L, row.get("ROOM_ID"));
        assertEquals(2, row.get("ADULTS"));
        assertEquals("COMPLETED", row.get("STATUS"));
        assertEquals("check-in preservado", row.get("NOTES"));
    }

    private void assertPreservedCheckOut(JdbcTemplate jdbcTemplate) {
        Map<String, Object> row = jdbcTemplate.queryForMap("select * from check_outs where id = 12");

        assertNull(row.get("BOOKING_ID"));
        assertEquals(7L, row.get("GUEST_ID"));
        assertEquals(1L, row.get("ROOM_ID"));
        assertEquals(true, row.get("KEYS_RETURNED"));
        assertEquals("COMPLETED", row.get("STATUS"));
        assertEquals("checkout preservado", row.get("NOTES"));
    }

    private int count(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.queryForObject("select count(*) from " + tableName, Integer.class);
    }

    private Long bookingId(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.queryForObject(
                "select booking_id from " + tableName + " where id in (11, 12)",
                Long.class
        );
    }
}
