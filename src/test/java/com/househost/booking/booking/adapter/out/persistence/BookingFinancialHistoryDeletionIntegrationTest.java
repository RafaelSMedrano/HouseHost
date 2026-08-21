package com.househost.booking.booking.adapter.out.persistence;

import java.util.Map;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingFinancialHistoryDeletionIntegrationTest {

    @Test
    void deletingBookingPreservesFinancialTransactionSourceHistory() {
        JdbcTemplate jdbcTemplate = fixtureDatabase();

        jdbcTemplate.update("delete from bookings where id = ?", 11L);

        assertEquals(0, count(jdbcTemplate, "bookings"));
        assertEquals(1, count(jdbcTemplate, "financial_transactions"));
        Map<String, Object> financialTransactionRowMap = jdbcTemplate.queryForMap(
                "select * from financial_transactions where id = 31"
        );
        assertEquals("BOOKING", financialTransactionRowMap.get("SOURCE_TYPE"));
        assertEquals(11L, financialTransactionRowMap.get("SOURCE_ID"));
        assertEquals("WAITING", financialTransactionRowMap.get("STATUS"));
        assertEquals("700.00", financialTransactionRowMap.get("AMOUNT").toString());
    }

    private JdbcTemplate fixtureDatabase() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL(
                "jdbc:h2:mem:financial_booking_history_"
                        + UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1"
        );
        JdbcTemplate jdbcTemplate = new JdbcTemplate(jdbcDataSource);
        jdbcTemplate.execute("create table bookings (id bigint primary key)");
        jdbcTemplate.execute("""
                create table financial_transactions (
                    id bigint primary key,
                    source_type varchar(30),
                    source_id bigint,
                    status varchar(30) not null,
                    amount decimal(19,2) not null
                )
                """);
        jdbcTemplate.update("insert into bookings (id) values (?)", 11L);
        jdbcTemplate.update("""
                insert into financial_transactions (
                    id,
                    source_type,
                    source_id,
                    status,
                    amount
                ) values (?, ?, ?, ?, ?)
                """, 31L, "BOOKING", 11L, "WAITING", 700.00);
        return jdbcTemplate;
    }

    private int count(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.queryForObject(
                "select count(*) from " + tableName,
                Integer.class
        );
    }
}
