package com.househost.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseSchemaCompatibilityRunner implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaCompatibilityRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!isMysql(connection)) {
                return;
            }
        }

        jdbcTemplate.execute("""
                alter table bookings
                modify column status enum('PENDING','CONFIRMED','CANCELLED','GOT_CHECKIN') not null
                """);
        ensureUserRoleColumn();
        ensureBookingOriginColumn();
        ensureGuestStatusColumn();
        ensureGuestTypeColumn();
        ensureGuestFinancialStatusColumn();
        syncGuestStatusFromStays();
        ensureFinancialStatusColumns();
        ensureFinancialTypeColumns();
        ensureFinancialPartyColumns();
        ensureFinancialTransactionSourceColumns();
        ensureLegacyFinancialCashierColumnsAreNullable();
        ensureFinancialAmountSigns();
        ensureCashierMovementSourceColumns();
        ensureCashierOnWaitingColumn();
        ensureDefaultPaymentCashier();
    }

    private void ensureUserRoleColumn() {
        if (!tableExists("users")) {
            return;
        }

        if (!columnExists("users", "photo_url")) {
            jdbcTemplate.execute("""
                    alter table users
                    add column photo_url longtext
                    """);
        }

        jdbcTemplate.execute("""
                alter table users
                modify column photo_url longtext
                """);

        if (!columnExists("users", "phone")) {
            jdbcTemplate.execute("""
                    alter table users
                    add column phone varchar(30)
                    """);
        }

        if (!columnExists("users", "role")) {
            jdbcTemplate.execute("""
                    alter table users
                    add column role enum('CEO','CTO','ADMIN','MANAGER','RECEPTION','HOUSEKEEPING') not null default 'RECEPTION'
                    """);
            return;
        }

        jdbcTemplate.execute("""
                alter table users
                modify column role varchar(40) not null default 'RECEPTION'
                """);

        jdbcTemplate.execute("""
                update users
                set role = case
                    when upper(role) = 'CEO' then 'CEO'
                    when upper(role) = 'CTO' then 'CTO'
                    when upper(role) in ('ADMIN','ADMINISTRADOR','ADMINISTRADORA') then 'ADMIN'
                    when upper(role) in ('MANAGER','GERENTE','GESTOR','GESTORA') then 'MANAGER'
                    when upper(role) in ('HOUSEKEEPING','GOVERNANCA','GOVERNANÇA','LIMPEZA') then 'HOUSEKEEPING'
                    when upper(role) in ('RECEPTION','RECEPCAO','RECEPÇÃO','RECEPCIONISTA') then 'RECEPTION'
                    else 'RECEPTION'
                end
                """);

        jdbcTemplate.execute("""
                alter table users
                modify column role enum('CEO','CTO','ADMIN','MANAGER','RECEPTION','HOUSEKEEPING') not null default 'RECEPTION'
                """);
    }

    private void ensureBookingOriginColumn() {
        if (!columnExists("bookings", "origin")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column origin enum('DIRETO_TELEFONE','WHATSAPP','INSTAGRAM','BOOKING','AIRBNB','INDICACAO') not null default 'DIRETO_TELEFONE'
                    """);
            return;
        }

        jdbcTemplate.execute("""
                alter table bookings
                modify column origin varchar(50) not null default 'DIRETO_TELEFONE'
                """);

        jdbcTemplate.execute("""
                update bookings
                set origin = case
                    when upper(replace(replace(replace(origin, ' ', ''), '-', ''), '/', '')) in ('DIRETOTELEFONE','DIRECTPHONE','DIRECT','TELEFONE','ON') then 'DIRETO_TELEFONE'
                    when upper(origin) in ('WHATSAPP','WHATS') then 'WHATSAPP'
                    when upper(origin) in ('INSTAGRAM','INSTA') then 'INSTAGRAM'
                    when upper(origin) = 'BOOKING' then 'BOOKING'
                    when upper(origin) = 'AIRBNB' then 'AIRBNB'
                    when upper(origin) in ('INDICACAO','INDICAÇÃO','INDICATION','REFERRAL') then 'INDICACAO'
                    else 'DIRETO_TELEFONE'
                end
                """);

        jdbcTemplate.execute("""
                alter table bookings
                modify column origin enum('DIRETO_TELEFONE','WHATSAPP','INSTAGRAM','BOOKING','AIRBNB','INDICACAO') not null default 'DIRETO_TELEFONE'
                """);
    }

    private void ensureGuestStatusColumn() {
        Integer statusColumns = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'guests'
                  and column_name = 'status'
                """, Integer.class);

        if (statusColumns == null || statusColumns == 0) {
            jdbcTemplate.execute("""
                    alter table guests
                    add column status enum('IN_BOOKING','IN_STAY','GOT_CHECKOUT') not null default 'IN_BOOKING'
                    """);
            return;
        }

        jdbcTemplate.execute("""
                alter table guests
                modify column status enum('COM_RESERVA','EM_ESTADIA','COM_CHECK_OUT','IN_BOOKING','IN_STAY','GOT_CHECKOUT') not null default 'IN_BOOKING'
                """);

        jdbcTemplate.execute("""
                update guests
                set status = case status
                    when 'COM_RESERVA' then 'IN_BOOKING'
                    when 'EM_ESTADIA' then 'IN_STAY'
                    when 'COM_CHECK_OUT' then 'GOT_CHECKOUT'
                    else status
                end
                """);

        jdbcTemplate.execute("""
                alter table guests
                modify column status enum('IN_BOOKING','IN_STAY','GOT_CHECKOUT') not null default 'IN_BOOKING'
                """);
    }

    private void syncGuestStatusFromStays() {
        jdbcTemplate.execute("""
                update guests guest
                set guest.status = 'IN_STAY'
                where exists (
                    select 1
                    from stays stay
                    where stay.guest_id = guest.id
                      and stay.status = 'ACTIVE'
                )
                """);

        jdbcTemplate.execute("""
                update guests guest
                set guest.status = 'GOT_CHECKOUT'
                where guest.status <> 'IN_STAY'
                  and exists (
                    select 1
                    from stays stay
                    where stay.guest_id = guest.id
                      and stay.status = 'CHECKED_OUT'
                  )
                """);
    }

    private void ensureGuestTypeColumn() {
        if (!columnExists("guests", "guest_type")) {
            jdbcTemplate.execute("""
                    alter table guests
                    add column guest_type enum('NOVO','REGULAR','VIP') not null default 'REGULAR'
                    """);
            return;
        }

        jdbcTemplate.execute("""
                alter table guests
                modify column guest_type varchar(30) not null default 'regular'
                """);

        jdbcTemplate.execute("""
                update guests
                set guest_type = case upper(guest_type)
                    when 'NEW' then 'NOVO'
                    when 'NOVO' then 'NOVO'
                    when 'VIP' then 'VIP'
                    else 'REGULAR'
                end
                """);

        jdbcTemplate.execute("""
                alter table guests
                modify column guest_type enum('NOVO','REGULAR','VIP') not null default 'REGULAR'
                """);
    }

    private void ensureGuestFinancialStatusColumn() {
        if (!columnExists("guests", "financial_status")) {
            jdbcTemplate.execute("""
                    alter table guests
                    add column financial_status enum('WAITING_PAYMENT','PAYMENT_SETTLED','DEBTOR') not null default 'PAYMENT_SETTLED'
                    """);
        }

        jdbcTemplate.execute("""
                update guests guest
                set guest.financial_status = case
                    when exists (
                        select 1
                        from financial_transactions ft
                        where ft.guest_id = guest.id
                          and ft.status <> 'SETTLED'
                          and ft.transaction_date < current_date()
                    ) then 'DEBTOR'
                    when (
                        select ft.status
                        from financial_transactions ft
                        where ft.guest_id = guest.id
                        order by ft.created_at desc, ft.id desc
                        limit 1
                    ) in ('SETTLED', 'PAID') then 'PAYMENT_SETTLED'
                    when (
                        select ft.status
                        from financial_transactions ft
                        where ft.guest_id = guest.id
                        order by ft.created_at desc, ft.id desc
                        limit 1
                    ) in ('WAITING', 'ON_TIME', 'PARTIALLY_REALIZED') then 'WAITING_PAYMENT'
                    when exists (
                        select 1
                        from financial_transactions ft
                        where ft.guest_id = guest.id
                    ) then 'DEBTOR'
                    else 'PAYMENT_SETTLED'
                end
                """);
    }

    private void ensureFinancialStatusColumns() {
        if (columnExists("financial_transactions", "status")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    modify column status enum('CONSUMED','SETTLED','WAITING','CANCELED','LATE','NOT_REALIZED','PARTIALLY_REALIZED','PAID','ON_TIME') not null
                    """);

            jdbcTemplate.execute("""
                    update financial_transactions
                    set status = 'SETTLED'
                    where status = 'CONSUMED'
                    """);

            jdbcTemplate.execute("""
                    alter table financial_transactions
                    modify column status enum('SETTLED','WAITING','CANCELED','LATE','NOT_REALIZED','PARTIALLY_REALIZED','PAID','ON_TIME') not null
                    """);
        }

        if (columnExists("installment_transactions", "installment_status")) {
            jdbcTemplate.execute("""
                    alter table installment_transactions
                    modify column installment_status enum('CONSUMED','SETTLED','WAITING','LATE','NOT_REALIZED') not null
                    """);

            jdbcTemplate.execute("""
                    update installment_transactions
                    set installment_status = 'SETTLED'
                    where installment_status = 'CONSUMED'
                    """);

            jdbcTemplate.execute("""
                    alter table installment_transactions
                    modify column installment_status enum('SETTLED','WAITING','LATE','NOT_REALIZED') not null
                    """);
        }

        ensureCashierMovementStatusColumn("cashier_entries");
        ensureCashierMovementStatusColumn("cashier_expenses");
    }

    private void ensureCashierMovementStatusColumn(String tableName) {
        if (columnExists(tableName, "status")) {
            jdbcTemplate.execute(String.format("""
                    alter table %s
                    modify column status enum('SETTLED','WAITING','CANCELED','LATE','NOT_REALIZED','PARTIALLY_REALIZED','PAID','ON_TIME') not null
                    """, tableName));
        }
    }

    private void ensureFinancialTypeColumns() {
        if (columnExists("financial_transactions", "type")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    modify column type enum('ENTRY','EXPENSE','TRANSFER') not null
                    """);
        }
    }

    private void ensureFinancialPartyColumns() {
        if (!columnExists("financial_transactions", "sender_type")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column sender_type enum('CASHIER','GUEST')
                    """);
        }

        if (!columnExists("financial_transactions", "sender_id")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column sender_id bigint
                    """);
        }

        if (!columnExists("financial_transactions", "receiver_type")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column receiver_type enum('CASHIER','GUEST')
                    """);
        }

        if (!columnExists("financial_transactions", "receiver_id")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column receiver_id bigint
                    """);
        }

        if (columnExists("financial_transactions", "sender_cashier_id")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set sender_type = 'CASHIER',
                        sender_id = sender_cashier_id
                    where sender_type is null
                      and sender_cashier_id is not null
                    """);
        }

        if (columnExists("financial_transactions", "receiver_cashier_id")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set receiver_type = 'CASHIER',
                        receiver_id = receiver_cashier_id
                    where receiver_type is null
                      and receiver_cashier_id is not null
                    """);
        }

        jdbcTemplate.execute("""
                update financial_transactions
                set sender_type = 'CASHIER',
                    sender_id = 1
                where sender_type is null
                   or sender_id is null
                """);

        jdbcTemplate.execute("""
                update financial_transactions
                set receiver_type = 'CASHIER',
                    receiver_id = 1
                where receiver_type is null
                   or receiver_id is null
                """);

        jdbcTemplate.execute("""
                alter table financial_transactions
                modify column sender_type enum('CASHIER','GUEST') not null,
                modify column sender_id bigint not null,
                modify column receiver_type enum('CASHIER','GUEST') not null,
                modify column receiver_id bigint not null
                """);
    }

    private void ensureFinancialTransactionSourceColumns() {
        if (!columnExists("financial_transactions", "source_type")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column source_type enum('MANUAL','BOOKING','STAY','CHECK_IN','CHECK_OUT','INSTALLMENT','GUEST')
                    """);
        }

        if (!columnExists("financial_transactions", "source_id")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column source_id bigint
                    """);
        }

        if (columnExists("financial_transactions", "booking_id")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set source_type = 'BOOKING',
                        source_id = booking_id
                    where source_type is null
                      and booking_id is not null
                    """);
        }

        jdbcTemplate.execute("""
                update financial_transactions transaction
                join bookings booking
                  on transaction.description = concat('Pagamento da reserva #', booking.id)
                set transaction.source_type = 'BOOKING',
                    transaction.source_id = booking.id
                where transaction.source_type is null
                """);
    }

    private void ensureLegacyFinancialCashierColumnsAreNullable() {
        makeBigIntColumnNullable("financial_transactions", "cashier_id");
        makeBigIntColumnNullable("financial_transactions", "sender_cashier_id");
        makeBigIntColumnNullable("financial_transactions", "receiver_cashier_id");
    }

    private void makeBigIntColumnNullable(String tableName, String columnName) {
        if (columnExists(tableName, columnName)) {
            jdbcTemplate.execute(String.format("""
                    alter table %s
                    modify column %s bigint null
                    """, tableName, columnName));
        }
    }

    private void ensureFinancialAmountSigns() {
        if (!columnExists("financial_transactions", "amount")
                || !columnExists("financial_transactions", "entry_amount")
                || !columnExists("financial_transactions", "expense_amount")
                || !columnExists("financial_transactions", "type")) {
            return;
        }

        jdbcTemplate.execute("""
                update financial_transactions
                set amount = abs(amount),
                    entry_amount = abs(amount),
                    expense_amount = 0
                where type = 'ENTRY'
                """);

        jdbcTemplate.execute("""
                update financial_transactions
                set amount = -abs(amount),
                    entry_amount = 0,
                    expense_amount = abs(amount)
                where type = 'EXPENSE'
                """);
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer columns = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, Integer.class, tableName, columnName);

        return columns != null && columns > 0;
    }

    private void ensureCashierMovementSourceColumns() {
        ensureCashierMovementSourceColumn("cashier_entries");
        ensureCashierMovementSourceColumn("cashier_expenses");
    }

    private void ensureCashierMovementSourceColumn(String tableName) {
        if (!columnExists(tableName, "source_transaction_id")) {
            jdbcTemplate.execute(String.format("""
                    alter table %s
                    add column source_transaction_id bigint
                    """, tableName));
        }

        if (columnExists(tableName, "financial_transaction_id")) {
            jdbcTemplate.execute(String.format("""
                    update %s
                    set source_transaction_id = financial_transaction_id
                    where source_transaction_id is null
                      and financial_transaction_id is not null
                    """, tableName));

            makeBigIntColumnNullable(tableName, "financial_transaction_id");
        }
    }

    private void ensureCashierOnWaitingColumn() {
        if (!columnExists("cashiers", "on_waiting")) {
            jdbcTemplate.execute("""
                    alter table cashiers
                    add column on_waiting decimal(19,2) not null default 0
                    """);
        }

        jdbcTemplate.execute("""
                update cashiers cashier
                set cashier.on_waiting =
                    coalesce((
                        select sum(entry.amount)
                        from cashier_entries entry
                        where entry.cashier_id = cashier.id
                          and entry.status = 'WAITING'
                    ), 0)
                    -
                    coalesce((
                        select sum(abs(expense.amount))
                        from cashier_expenses expense
                        where expense.cashier_id = cashier.id
                          and expense.status = 'WAITING'
                    ), 0)
                """);
    }

    private void ensureDefaultPaymentCashier() {
        if (!tableExists("cashiers")) {
            return;
        }

        Integer cashierColumns = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'cashiers'
                  and column_name in (
                    'id',
                    'name',
                    'opening_balance',
                    'cash_on_hand',
                    'on_waiting',
                    'expected_inflow',
                    'expected_outflow',
                    'total_inflow',
                    'total_outflow',
                    'status',
                    'created_at',
                    'updated_at'
                  )
                """, Integer.class);

        if (cashierColumns == null || cashierColumns < 12) {
            return;
        }

        Integer defaultCashiers = jdbcTemplate.queryForObject("""
                select count(*)
                from cashiers
                where id = 1
                """, Integer.class);

        if (defaultCashiers != null && defaultCashiers > 0) {
            return;
        }

        jdbcTemplate.execute("""
                insert into cashiers (
                    id,
                    name,
                    description,
                    opening_balance,
                    cash_on_hand,
                    on_waiting,
                    expected_inflow,
                    expected_outflow,
                    total_inflow,
                    total_outflow,
                    status,
                    created_at,
                    updated_at
                ) values (
                    1,
                    'Caixa Principal #1',
                    'Caixa padrao para recebimentos de reservas',
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    'OPEN',
                    now(),
                    now()
                )
                """);
    }

    private boolean tableExists(String tableName) {
        Integer tables = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = database()
                  and table_name = ?
                """, Integer.class, tableName);

        return tables != null && tables > 0;
    }

    private boolean isMysql(Connection connection) throws Exception {
        return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql");
    }
}
