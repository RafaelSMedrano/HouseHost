package com.househost.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Component
@Order(0)
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
                modify column status enum(
                    'PENDING','CONFIRMED','CANCELLED','GOT_CHECKIN','GOT_CHECKOUT',
                    'UNCONFIRMED','IN_STAY','FINISHED','CANCELED'
                ) not null
                """);
        jdbcTemplate.execute("""
                update bookings
                set status = case status
                    when 'PENDING' then 'UNCONFIRMED'
                    when 'CANCELLED' then 'CANCELED'
                    when 'GOT_CHECKIN' then 'IN_STAY'
                    when 'GOT_CHECKOUT' then 'FINISHED'
                    else status
                end
                """);
        jdbcTemplate.execute("""
                alter table bookings
                modify column status enum('UNCONFIRMED','CONFIRMED','IN_STAY','FINISHED','CANCELED') not null
                """);
        ensureUserRoleColumn();
        ensureBookingOriginColumn();
        ensureBookingPaymentStatusColumn();
        ensureBookingPrivacyAcceptanceColumns();
        ensureRatingsTable();
        removeLegacyCheckInDateColumns();
        ensureDataProcessingOperationsTable();
        ensureProcessingLegalBasisAssessmentsTable();
        ensurePrivacyPoliciesTable();
        ensureAuditEventsTable();
        ensureLoginSecurityControlsTable();
        ensureSupplierTables();
        ensureGuestCareStorage();
        removeObsoleteGuestReferralColumn();
        ensureGuestStatusColumn();
        ensureGuestTypeColumn();
        ensureGuestFinancialStatusColumn();
        removeLegacyStaySchema();
        ensureStayHistoryBookingLinksAreOptional();
        ensureCheckOutGuestHistoryColumns();
        removeObsoleteGenericRatingColumns();
        ensureFinancialStatusColumns();
        ensureFinancialPartyColumns();
        ensureFinancialTransactionSourceColumns();
        ensureLegacyFinancialCashierColumnsAreNullable();
        migrateFinancialTransactionClassification();
        ensureFinancialTransactionPlanSchema();
        ensureFinancialCommandIdempotencySchema();
        ensureNotifierSchema();
        ensureCashierMovementSourceColumns();
        ensureCashierMovementTemporalColumns();
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

    private void removeLegacyCheckInDateColumns() {
        if (!tableExists("check_ins")) {
            return;
        }
        if (columnExists("check_ins", "expected_arrival_at")) {
            jdbcTemplate.execute("alter table check_ins drop column expected_arrival_at");
        }
        if (columnExists("check_ins", "actual_check_in_at")) {
            jdbcTemplate.execute("alter table check_ins drop column actual_check_in_at");
        }
        if (columnExists("check_ins", "expected_check_out_date")) {
            jdbcTemplate.execute("alter table check_ins drop column expected_check_out_date");
        }
        if (columnExists("check_ins", "updated_at")) {
            jdbcTemplate.execute("alter table check_ins drop column updated_at");
        }
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

    private void ensureBookingPaymentStatusColumn() {
        if (!tableExists("bookings")) {
            return;
        }

        if (!columnExists("bookings", "payment_status")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column payment_status varchar(20) not null default 'WAITING'
                    """);

            jdbcTemplate.execute("""
                    update bookings
                    set payment_status = case
                        when coalesce(total_amount, 0) > 0
                             and coalesce(paid_amount, 0) >= total_amount then 'PAID'
                        when coalesce(paid_amount, 0) > 0 then 'PARTIAL'
                        else 'WAITING'
                    end
                    """);
        }

        jdbcTemplate.execute("""
                update bookings
                set payment_status = 'WAITING'
                where payment_status is null
                   or payment_status not in ('WAITING','PARTIAL','PAID')
                """);

        jdbcTemplate.execute("""
                alter table bookings
                modify column payment_status enum('WAITING','PARTIAL','PAID') not null default 'WAITING'
                """);
    }

    private void ensureBookingPrivacyAcceptanceColumns() {
        if (!tableExists("bookings")) {
            return;
        }

        if (!columnExists("bookings", "privacy_policy_version")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column privacy_policy_version varchar(120)
                    """);
        }

        if (!columnExists("bookings", "privacy_policy_content_hash")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column privacy_policy_content_hash varchar(71)
                    """);
        }

        if (!columnExists("bookings", "terms_version")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column terms_version varchar(120)
                    """);
        }

        if (!columnExists("bookings", "privacy_accepted_at")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column privacy_accepted_at datetime(6)
                    """);
        }

        if (!columnExists("bookings", "marketing_opt_in")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column marketing_opt_in bit not null default 0
                    """);
        }

        jdbcTemplate.execute("""
                update bookings
                set marketing_opt_in = 0
                where marketing_opt_in is null
                """);

        jdbcTemplate.execute("""
                alter table bookings
                modify column marketing_opt_in bit not null default 0
                """);

        if (!columnExists("bookings", "marketing_opt_in_at")) {
            jdbcTemplate.execute("""
                    alter table bookings
                    add column marketing_opt_in_at datetime(6)
                    """);
        }
    }

    void ensureRatingsTable() {
        if (!tableExists("bookings")) {
            return;
        }

        jdbcTemplate.execute("""
                create table if not exists ratings (
                    id bigint not null auto_increment,
                    booking_id bigint not null,
                    check_in_procedure_score int not null,
                    check_out_procedure_score int not null,
                    accommodation_cleanliness_score int not null,
                    team_communication_score int not null,
                    location_score int not null,
                    comfort_score int not null,
                    observations text null,
                    evaluated_at datetime(6) not null,
                    created_at datetime(6) not null,
                    updated_at datetime(6) not null,
                    primary key (id),
                    constraint uk_ratings_booking unique (booking_id),
                    constraint ck_ratings_check_in_procedure
                        check (check_in_procedure_score between 1 and 5),
                    constraint ck_ratings_check_out_procedure
                        check (check_out_procedure_score between 1 and 5),
                    constraint ck_ratings_accommodation_cleanliness
                        check (accommodation_cleanliness_score between 1 and 5),
                    constraint ck_ratings_team_communication
                        check (team_communication_score between 1 and 5),
                    constraint ck_ratings_location
                        check (location_score between 1 and 5),
                    constraint ck_ratings_comfort
                        check (comfort_score between 1 and 5),
                    constraint fk_ratings_booking
                        foreign key (booking_id) references bookings(id)
                )
                """);
    }

    private void ensureAuditEventsTable() {
        if (!tableExists("audit_events")) {
            jdbcTemplate.execute("""
                    create table audit_events (
                        id bigint not null auto_increment,
                        event_type varchar(80) not null,
                        entity_type varchar(80) not null,
                        entity_id bigint,
                        processing_operation_id bigint,
                        actor_type varchar(80) not null,
                        actor_id bigint,
                        actor_label varchar(180),
                        occurred_at datetime(6) not null,
                        ip_address varchar(80),
                        user_agent varchar(500),
                        metadata_json longtext,
                        created_at datetime(6) not null default current_timestamp(6),
                        primary key (id),
                        index idx_audit_events_entity (entity_type, entity_id),
                        index idx_audit_events_processing_operation (processing_operation_id),
                        index idx_audit_events_type_time (event_type, occurred_at),
                        index idx_audit_events_actor (actor_type, actor_id),
                        constraint fk_audit_events_processing_operation
                            foreign key (processing_operation_id)
                            references data_processing_operations (id)
                    )
                    """);
            return;
        }

        ensureAuditColumn("event_type", "varchar(80) not null");
        ensureAuditColumn("entity_type", "varchar(80) not null");
        ensureAuditColumn("entity_id", "bigint");
        ensureAuditColumn("processing_operation_id", "bigint");
        ensureAuditColumn("actor_type", "varchar(80) not null");
        ensureAuditColumn("actor_id", "bigint");
        ensureAuditColumn("actor_label", "varchar(180)");
        ensureAuditColumn("occurred_at", "datetime(6) not null");
        ensureAuditColumn("ip_address", "varchar(80)");
        ensureAuditColumn("user_agent", "varchar(500)");
        ensureAuditColumn("metadata_json", "longtext");
        ensureAuditColumn("created_at", "datetime(6) not null default current_timestamp(6)");
        ensureAuditProcessingOperationIndex();
        ensureAuditProcessingOperationForeignKey();
    }

    private void ensureLoginSecurityControlsTable() {
        if (!tableExists("login_attempt_controls")) {
            jdbcTemplate.execute("""
                    create table login_attempt_controls (
                        id bigint not null auto_increment,
                        scope_type varchar(20) not null,
                        scope_key varchar(64) not null,
                        failure_count int not null default 0,
                        window_started_at datetime(6),
                        last_failed_at datetime(6),
                        blocked_until datetime(6),
                        version bigint not null default 0,
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        primary key (id),
                        unique key uk_login_attempt_scope_key (scope_type, scope_key),
                        index idx_login_attempt_updated_at (updated_at)
                    )
                    """);
            return;
        }
        if (!indexExists("login_attempt_controls", "uk_login_attempt_scope_key")) {
            jdbcTemplate.execute("""
                    create unique index uk_login_attempt_scope_key
                    on login_attempt_controls (scope_type, scope_key)
                    """);
        }
        if (!indexExists("login_attempt_controls", "idx_login_attempt_updated_at")) {
            jdbcTemplate.execute("""
                    create index idx_login_attempt_updated_at
                    on login_attempt_controls (updated_at)
                    """);
        }
    }

    private void ensureSupplierTables() {
        if (!tableExists("suppliers")) {
            jdbcTemplate.execute("""
                    create table suppliers (
                        id bigint not null auto_increment,
                        official_name varchar(180) not null,
                        normalized_official_name varchar(180) not null,
                        trade_name varchar(180),
                        registration_identifier varchar(80),
                        website varchar(300),
                        country_of_establishment varchar(120) not null,
                        business_contact varchar(300),
                        privacy_contact varchar(300),
                        incident_contact varchar(300),
                        internal_owner_user_id bigint,
                        status varchar(20) not null,
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        version bigint not null default 0,
                        primary key (id),
                        unique key uk_supplier_normalized_name (normalized_official_name),
                        unique key uk_supplier_registration_identifier (registration_identifier),
                        index idx_supplier_status (status)
                    )
                    """);
        }
        if (!tableExists("supplier_data_processing_relationships")) {
            jdbcTemplate.execute("""
                    create table supplier_data_processing_relationships (
                        id bigint not null auto_increment,
                        supplier_id bigint not null,
                        service_name varchar(180) not null,
                        description longtext,
                        purpose longtext,
                        personal_data_categories longtext,
                        data_subject_categories longtext,
                        processing_actions longtext,
                        role varchar(40) not null,
                        role_assessment longtext,
                        storage_locations longtext,
                        international_transfer bit not null default 0,
                        transfer_mechanism longtext,
                        retention_criteria longtext,
                        deletion_or_return_procedure longtext,
                        security_measures longtext,
                        incident_notification_channel varchar(300),
                        incident_notification_expectation longtext,
                        sub_operator_information longtext,
                        contract_status varchar(30) not null,
                        contract_reference varchar(300),
                        contract_start_date date,
                        contract_end_date date,
                        responsibility_summary longtext,
                        risk_level varchar(20) not null,
                        governance_status varchar(20) not null,
                        assessment_notes longtext,
                        reviewed_at datetime(6),
                        reviewed_by_user_id bigint,
                        next_review_date date,
                        ended_at date,
                        data_disposition_status varchar(40) not null,
                        data_disposition_notes longtext,
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        version bigint not null default 0,
                        primary key (id),
                        index idx_supplier_relationship_supplier (supplier_id),
                        index idx_supplier_relationship_role (role),
                        index idx_supplier_relationship_risk (risk_level),
                        index idx_supplier_relationship_governance (governance_status),
                        index idx_supplier_relationship_next_review (next_review_date),
                        constraint fk_supplier_relationship_supplier foreign key (supplier_id) references suppliers (id)
                    )
                    """);
        }
        ensureSupplierIndex("suppliers", "idx_supplier_status", "status");
        ensureSupplierIndex("supplier_data_processing_relationships", "idx_supplier_relationship_role", "role");
        ensureSupplierIndex("supplier_data_processing_relationships", "idx_supplier_relationship_risk", "risk_level");
        ensureSupplierIndex("supplier_data_processing_relationships", "idx_supplier_relationship_governance", "governance_status");
        ensureSupplierIndex("supplier_data_processing_relationships", "idx_supplier_relationship_next_review", "next_review_date");
    }

    private void ensureSupplierIndex(String tableName, String indexName, String columnName) {
        if (!indexExists(tableName, indexName)) {
            jdbcTemplate.execute(String.format("create index %s on %s (%s)", indexName, tableName, columnName));
        }
    }

    private void ensureAuditColumn(String columnName, String definition) {
        if (!columnExists("audit_events", columnName)) {
            jdbcTemplate.execute(String.format("""
                    alter table audit_events
                    add column %s %s
                    """, columnName, definition));
        }
    }

    private void ensureAuditProcessingOperationIndex() {
        if (!indexExists("audit_events", "idx_audit_events_processing_operation")) {
            jdbcTemplate.execute("""
                    create index idx_audit_events_processing_operation
                    on audit_events (processing_operation_id)
                    """);
        }
    }

    private void ensureAuditProcessingOperationForeignKey() {
        if (!foreignKeyExists("audit_events", "processing_operation_id")) {
            jdbcTemplate.execute("""
                    alter table audit_events
                    add constraint fk_audit_events_processing_operation
                    foreign key (processing_operation_id)
                    references data_processing_operations (id)
                    """);
        }
    }

    private void ensureDataProcessingOperationsTable() {
        if (!tableExists("data_processing_operations")) {
            jdbcTemplate.execute("""
                    create table data_processing_operations (
                        id bigint not null auto_increment,
                        operation_code varchar(80) not null,
                        operation_name varchar(180) not null,
                        description text not null,
                        purpose text not null,
                        legal_basis varchar(80) not null,
                        data_subject_categories text not null,
                        personal_data_categories text not null,
                        data_source text not null,
                        processing_actions text not null,
                        internal_access_roles text not null,
                        external_recipients text,
                        international_transfer bit not null default 0,
                        retention_period text not null,
                        deletion_method text not null,
                        security_measures text not null,
                        responsible_area varchar(120) not null,
                        system_name varchar(120) not null,
                        status varchar(20) not null default 'ACTIVE',
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        reviewed_at datetime(6),
                        reviewed_by_user_id bigint,
                        primary key (id),
                        unique key uk_data_processing_operation_code (operation_code),
                        unique key uk_data_processing_operation_name (operation_name),
                        index idx_data_processing_operation_status (status),
                        index idx_data_processing_operation_reviewer (reviewed_by_user_id)
                    )
                    """);
            return;
        }

        ensureDataProcessingOperationColumn("operation_code", "varchar(80)");
        ensureDataProcessingOperationColumn("operation_name", "varchar(180) not null");
        ensureDataProcessingOperationColumn("description", "text not null");
        ensureDataProcessingOperationColumn("purpose", "text not null");
        ensureDataProcessingOperationColumn("legal_basis", "varchar(80) not null");
        ensureDataProcessingOperationColumn("data_subject_categories", "text not null");
        ensureDataProcessingOperationColumn("personal_data_categories", "text not null");
        ensureDataProcessingOperationColumn("data_source", "text not null");
        ensureDataProcessingOperationColumn("processing_actions", "text not null");
        ensureDataProcessingOperationColumn("internal_access_roles", "text not null");
        ensureDataProcessingOperationColumn("external_recipients", "text");
        ensureDataProcessingOperationColumn("international_transfer", "bit not null default 0");
        ensureDataProcessingOperationColumn("retention_period", "text not null");
        ensureDataProcessingOperationColumn("deletion_method", "text not null");
        ensureDataProcessingOperationColumn("security_measures", "text not null");
        ensureDataProcessingOperationColumn("responsible_area", "varchar(120) not null");
        ensureDataProcessingOperationColumn("system_name", "varchar(120) not null");
        ensureDataProcessingOperationColumn("status", "varchar(20) not null default 'ACTIVE'");
        ensureDataProcessingOperationColumn("created_at", "datetime(6) not null");
        ensureDataProcessingOperationColumn("updated_at", "datetime(6) not null");
        ensureDataProcessingOperationColumn("reviewed_at", "datetime(6)");
        ensureDataProcessingOperationColumn("reviewed_by_user_id", "bigint");
        backfillDataProcessingOperationCodes();
        jdbcTemplate.execute("""
                alter table data_processing_operations
                modify column operation_code varchar(80) not null
                """);
        if (!indexExists("data_processing_operations", "uk_data_processing_operation_code")) {
            jdbcTemplate.execute("""
                    create unique index uk_data_processing_operation_code
                    on data_processing_operations (operation_code)
                    """);
        }
    }

    private void ensureProcessingLegalBasisAssessmentsTable() {
        if (!tableExists("processing_legal_basis_assessments")) {
            jdbcTemplate.execute("""
                    create table processing_legal_basis_assessments (
                        id bigint not null auto_increment,
                        processing_operation_id bigint not null,
                        purpose_key varchar(500) not null,
                        purpose text not null,
                        legal_basis varchar(50) not null,
                        justification text,
                        personal_data_categories text,
                        necessity_assessment text,
                        legal_reference text,
                        legal_obligation_description text,
                        contractual_context text,
                        consent_collection_mechanism text,
                        consent_evidence_mechanism text,
                        consent_withdrawal_mechanism text,
                        legitimate_interest text,
                        legitimate_expectation text,
                        rights_impact_assessment text,
                        safeguards text,
                        balancing_conclusion text,
                        sensitive_data bit not null default 0,
                        sensitive_data_legal_basis varchar(60),
                        sensitive_data_indispensability text,
                        status varchar(30) not null,
                        assessment_version integer not null,
                        previous_version_id bigint,
                        reviewed_by_user_id bigint,
                        submitted_at datetime(6),
                        reviewed_at datetime(6),
                        rejection_reason text,
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        primary key (id),
                        unique key uk_legal_basis_operation_purpose_version
                            (processing_operation_id, purpose_key, assessment_version),
                        index idx_legal_basis_operation_status (processing_operation_id, status),
                        index idx_legal_basis_previous_version (previous_version_id),
                        constraint fk_legal_basis_processing_operation
                            foreign key (processing_operation_id) references data_processing_operations (id),
                        constraint fk_legal_basis_previous_version
                            foreign key (previous_version_id) references processing_legal_basis_assessments (id)
                    )
                    """);
        }
        if (!indexExists("processing_legal_basis_assessments",
                "uk_legal_basis_operation_purpose_version")) {
            jdbcTemplate.execute("""
                    create unique index uk_legal_basis_operation_purpose_version
                    on processing_legal_basis_assessments
                        (processing_operation_id, purpose_key, assessment_version)
                    """);
        }
        if (!indexExists("processing_legal_basis_assessments", "idx_legal_basis_operation_status")) {
            jdbcTemplate.execute("""
                    create index idx_legal_basis_operation_status
                    on processing_legal_basis_assessments (processing_operation_id, status)
                    """);
        }
        if (!indexExists("processing_legal_basis_assessments", "idx_legal_basis_previous_version")) {
            jdbcTemplate.execute("""
                    create index idx_legal_basis_previous_version
                    on processing_legal_basis_assessments (previous_version_id)
                    """);
        }
        if (!foreignKeyExists("processing_legal_basis_assessments", "processing_operation_id")) {
            jdbcTemplate.execute("""
                    alter table processing_legal_basis_assessments
                    add constraint fk_legal_basis_processing_operation
                    foreign key (processing_operation_id) references data_processing_operations (id)
                    """);
        }
        if (!foreignKeyExists("processing_legal_basis_assessments", "previous_version_id")) {
            jdbcTemplate.execute("""
                    alter table processing_legal_basis_assessments
                    add constraint fk_legal_basis_previous_version
                    foreign key (previous_version_id) references processing_legal_basis_assessments (id)
                    """);
        }
    }

    private void ensurePrivacyPoliciesTable() {
        if (!tableExists("privacy_policies")) {
            jdbcTemplate.execute("""
                    create table privacy_policies (
                        id bigint not null auto_increment,
                        version integer not null,
                        title varchar(180) not null,
                        content longtext not null,
                        content_hash varchar(71),
                        status varchar(20) not null,
                        effective_at datetime(6) not null,
                        published_at datetime(6),
                        published_by_user_id bigint,
                        current_slot varchar(20),
                        created_at datetime(6) not null,
                        updated_at datetime(6) not null,
                        primary key (id),
                        unique key uk_privacy_policy_version (version),
                        unique key uk_privacy_policy_current_slot (current_slot),
                        index idx_privacy_policy_status (status)
                    )
                    """);
            return;
        }
        ensurePrivacyPolicyColumn("content_hash", "varchar(71)");
        ensurePrivacyPolicyColumn("published_at", "datetime(6)");
        ensurePrivacyPolicyColumn("published_by_user_id", "bigint");
        ensurePrivacyPolicyColumn("current_slot", "varchar(20)");
        if (!indexExists("privacy_policies", "uk_privacy_policy_version")) {
            jdbcTemplate.execute("""
                    create unique index uk_privacy_policy_version
                    on privacy_policies (version)
                    """);
        }
        if (!indexExists("privacy_policies", "uk_privacy_policy_current_slot")) {
            jdbcTemplate.execute("""
                    create unique index uk_privacy_policy_current_slot
                    on privacy_policies (current_slot)
                    """);
        }
        if (!indexExists("privacy_policies", "idx_privacy_policy_status")) {
            jdbcTemplate.execute("""
                    create index idx_privacy_policy_status
                    on privacy_policies (status)
                    """);
        }
    }

    private void ensurePrivacyPolicyColumn(String columnName, String definition) {
        if (!columnExists("privacy_policies", columnName)) {
            jdbcTemplate.execute(String.format("""
                    alter table privacy_policies
                    add column %s %s
                    """, columnName, definition));
        }
    }

    private void ensureDataProcessingOperationColumn(String columnName, String definition) {
        if (!columnExists("data_processing_operations", columnName)) {
            jdbcTemplate.execute(String.format("""
                    alter table data_processing_operations
                    add column %s %s
                    """, columnName, definition));
        }
    }

    private void backfillDataProcessingOperationCodes() {
        jdbcTemplate.update("""
                update data_processing_operations
                set operation_code = case operation_name
                    when 'Gestao de reservas' then 'BOOKING_MANAGEMENT'
                    when 'Gestao cadastral de hospedes' then 'GUEST_MANAGEMENT'
                    when 'Gestao de hospedagem, check-in e check-out' then 'STAY_MANAGEMENT'
                    when 'Gestao financeira de hospedagens' then 'FINANCIAL_MANAGEMENT'
                    when 'Marketing por WhatsApp' then 'WHATSAPP_MARKETING'
                    when 'Gestao de usuarios e controle de acesso' then 'USER_ACCESS_MANAGEMENT'
                    when 'Governanca de fornecedores e operadores' then 'SUPPLIER_GOVERNANCE'
                    when 'Seguranca, auditoria e resposta a incidentes' then 'SECURITY_AUDIT_MANAGEMENT'
                    when 'Governanca de privacidade e bases legais' then 'PRIVACY_GOVERNANCE'
                    else concat('CUSTOM_', id)
                end
                where operation_code is null
                   or trim(operation_code) = ''
                """);
    }

    void ensureGuestCareStorage() {
        if (!tableExists("guests")) {
            return;
        }

        ensureGuestTextColumn("preferences_and_restrictions");
        ensureGuestTextColumn("accessibility_needs");

        if (tableExists("guest_preferences")) {
            jdbcTemplate.execute("drop table guest_preferences");
        }

        dropGuestColumnIfPresent("travels_with_pets");
        dropGuestColumnIfPresent("pet_type");
        dropGuestColumnIfPresent("favorite_room");
        dropGuestColumnIfPresent("needs_accessibility");
    }

    void removeObsoleteGuestReferralColumn() {
        if (!tableExists("guests")) {
            return;
        }

        dropGuestColumnIfPresent("referred_by");
    }

    private void ensureGuestTextColumn(String columnName) {
        if (!columnExists("guests", columnName)) {
            jdbcTemplate.execute("alter table guests add column "
                    + sqlIdentifier(columnName) + " text null");
        }
    }

    private void dropGuestColumnIfPresent(String columnName) {
        if (columnExists("guests", columnName)) {
            jdbcTemplate.execute("alter table guests drop column " + sqlIdentifier(columnName));
        }
    }

    void ensureGuestStatusColumn() {
        if (!tableExists("guests")) {
            return;
        }

        if (!columnExists("guests", "status")) {
            jdbcTemplate.execute("""
                    alter table guests
                    add column status enum(
                        'WITH_UNCONFIRMED_BOOKING',
                        'WITH_CONFIRMED_BOOKING',
                        'IN_STAY',
                        'INACTIVE'
                    ) not null default 'INACTIVE'
                    """);
            syncGuestStatusFromBookings();
            return;
        }

        if (guestStatusColumnIsCompatible()) {
            syncGuestStatusFromBookings();
            return;
        }

        jdbcTemplate.execute("""
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
                """);

        syncGuestStatusFromBookings();

        jdbcTemplate.execute("""
                alter table guests
                modify column status enum(
                    'WITH_UNCONFIRMED_BOOKING',
                    'WITH_CONFIRMED_BOOKING',
                    'IN_STAY',
                    'INACTIVE'
                ) not null default 'INACTIVE'
                """);
    }

    private boolean guestStatusColumnIsCompatible() {
        String guestStatusDefinition = jdbcTemplate.queryForObject("""
                select concat(column_type, '|', is_nullable, '|', coalesce(column_default, ''))
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, String.class, "guests", "status");

        String expectedGuestStatusDefinition =
                "enum('WITH_UNCONFIRMED_BOOKING','WITH_CONFIRMED_BOOKING',"
                        + "'IN_STAY','INACTIVE')|NO|INACTIVE";
        return expectedGuestStatusDefinition.equalsIgnoreCase(guestStatusDefinition);
    }

    void syncGuestStatusFromBookings() {
        if (!tableExists("bookings")
                || !columnExists("bookings", "guest_id")
                || !columnExists("bookings", "status")) {
            jdbcTemplate.execute("update guests set status = 'INACTIVE'");
            return;
        }

        jdbcTemplate.execute("""
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
                """);
    }

    private void removeLegacyStaySchema() {
        boolean legacyTableExists = tableExists("stays");
        if (legacyTableExists && !columnExists("check_outs", "booking_id")) {
            jdbcTemplate.execute("alter table check_outs add column booking_id bigint null");
        }
        if (legacyTableExists && columnExists("check_outs", "stay_id")) {
            jdbcTemplate.execute("""
                    update check_outs check_out
                    join stays stay on stay.id = check_out.stay_id
                    set check_out.booking_id = stay.booking_id
                    where check_out.booking_id is null
                    """);
        }
        if (legacyTableExists) {
            boolean checkInHasStayId = columnExists("check_ins", "stay_id");
            boolean checkOutHasStayId = columnExists("check_outs", "stay_id");
            List<String> checkInStayConstraints = foreignKeysForColumn("check_ins", "stay_id");
            List<String> checkOutStayConstraints = foreignKeysForColumn("check_outs", "stay_id");
            jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
                try (var statement = connection.createStatement()) {
                    statement.execute("set foreign_key_checks = 0");
                    try {
                        for (String constraint : checkInStayConstraints) {
                            statement.execute("alter table check_ins drop foreign key `" + constraint + "`");
                        }
                        for (String constraint : checkOutStayConstraints) {
                            statement.execute("alter table check_outs drop foreign key `" + constraint + "`");
                        }
                        if (checkInHasStayId) {
                            statement.execute("alter table check_ins drop column stay_id");
                        }
                        if (checkOutHasStayId) {
                            statement.execute("alter table check_outs drop column stay_id");
                        }
                        statement.execute("drop table stays");
                    } finally {
                        statement.execute("set foreign_key_checks = 1");
                    }
                }
                return null;
            });
        }
    }

    void ensureStayHistoryBookingLinksAreOptional() {
        ensureOptionalBookingLink("check_ins", "fk_check_ins_booking");
        ensureOptionalBookingLink("check_outs", "fk_check_outs_booking");
    }

    void ensureCheckOutGuestHistoryColumns() {
        if (!tableExists("check_outs")) {
            return;
        }

        if (!columnExists("check_outs", "guest_history_applied")) {
            jdbcTemplate.execute(
                    "alter table check_outs add column guest_history_applied bit not null default 0"
            );
        }

        jdbcTemplate.execute("""
                update check_outs
                set guest_history_applied = 1
                where status = 'COMPLETED'
                  and guest_history_applied = 0
                """);
    }

    void removeObsoleteGenericRatingColumns() {
        dropColumnIfExists("guests", "rating");
        dropColumnIfExists("check_outs", "rating");
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        if (tableExists(tableName) && columnExists(tableName, columnName)) {
            jdbcTemplate.execute(
                    "alter table " + sqlIdentifier(tableName)
                            + " drop column " + sqlIdentifier(columnName)
            );
        }
    }

    private void ensureOptionalBookingLink(String tableName, String replacementConstraintName) {
        if (!tableExists("bookings")
                || !tableExists(tableName)
                || !columnExists(tableName, "booking_id")) {
            return;
        }

        List<String> constraintNameList = foreignKeysForColumn(tableName, "booking_id");
        boolean compatibleForeignKey = constraintNameList.size() == 1
                && columnIsNullable(tableName, "booking_id")
                && bookingForeignKeyUsesSetNull(tableName, "booking_id");
        if (compatibleForeignKey) {
            return;
        }

        for (String constraintName : constraintNameList) {
            jdbcTemplate.execute("alter table " + sqlIdentifier(tableName)
                    + " drop foreign key " + sqlIdentifier(constraintName));
        }

        jdbcTemplate.execute("alter table " + sqlIdentifier(tableName)
                + " modify column booking_id bigint null");
        jdbcTemplate.execute("alter table " + sqlIdentifier(tableName)
                + " add constraint " + sqlIdentifier(replacementConstraintName)
                + " foreign key (booking_id) references bookings(id) on delete set null");
    }

    private boolean columnIsNullable(String tableName, String columnName) {
        String nullability = jdbcTemplate.queryForObject("""
                select is_nullable
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, String.class, tableName, columnName);

        return "YES".equalsIgnoreCase(nullability);
    }

    private boolean bookingForeignKeyUsesSetNull(String tableName, String columnName) {
        Integer compatibleConstraintCount = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.key_column_usage column_usage
                join information_schema.referential_constraints referential_constraint
                  on referential_constraint.constraint_schema = column_usage.constraint_schema
                 and referential_constraint.constraint_name = column_usage.constraint_name
                where column_usage.table_schema = database()
                  and column_usage.table_name = ?
                  and column_usage.column_name = ?
                  and column_usage.referenced_table_name = 'bookings'
                  and column_usage.referenced_column_name = 'id'
                  and referential_constraint.delete_rule = 'SET NULL'
                """, Integer.class, tableName, columnName);

        return compatibleConstraintCount != null && compatibleConstraintCount == 1;
    }

    private String sqlIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_$]+")) {
            throw new IllegalArgumentException("Identificador SQL inválido");
        }
        return "`" + identifier + "`";
    }

    private List<String> foreignKeysForColumn(String table, String column) {
        return jdbcTemplate.queryForList("""
                select constraint_name
                from information_schema.key_column_usage
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                  and referenced_table_name is not null
                """, String.class, table, column);
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
                    ) = 'SETTLED' then 'PAYMENT_SETTLED'
                    when (
                        select ft.status
                        from financial_transactions ft
                        where ft.guest_id = guest.id
                        order by ft.created_at desc, ft.id desc
                        limit 1
                    ) = 'WAITING' then 'WAITING_PAYMENT'
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
                    modify column status enum('CONSUMED','SETTLED','WAITING','CANCELED','LATE','NOT_REALIZED','PARTIALLY_REALIZED','PAID','ON_TIME','OVERDUE') not null
                    """);

            jdbcTemplate.execute("""
                    update financial_transactions
                    set status = case
                        when status in ('CONSUMED', 'PAID') then 'SETTLED'
                        when status = 'NOT_REALIZED' then 'CANCELED'
                        when status = 'LATE' then 'OVERDUE'
                        else 'WAITING'
                    end
                    where status not in ('SETTLED', 'WAITING', 'CANCELED', 'ON_TIME', 'OVERDUE')
                    """);

            jdbcTemplate.execute("""
                    alter table financial_transactions
                    modify column status enum('SETTLED','WAITING','CANCELED','ON_TIME','OVERDUE') not null
                    """);
        }

        if (columnExists("installment_transactions", "installment_status")) {
            jdbcTemplate.execute("""
                    alter table installment_transactions
                    modify column installment_status enum('CONSUMED','SETTLED','WAITING','OVERDUE','LATE','NOT_REALIZED') not null
                    """);

            jdbcTemplate.execute("""
                    update installment_transactions
                    set installment_status = case
                        when installment_status = 'CONSUMED' then 'SETTLED'
                        when installment_status in ('LATE', 'NOT_REALIZED') then 'OVERDUE'
                        else installment_status
                    end
                    where installment_status in ('CONSUMED', 'LATE', 'NOT_REALIZED')
                    """);

            jdbcTemplate.execute("""
                    alter table installment_transactions
                    modify column installment_status enum('WAITING','SETTLED','OVERDUE') not null
                    """);
        }

        if (columnExists("installment_plan_transactions", "installment_plan_status")) {
            jdbcTemplate.execute("""
                    alter table installment_plan_transactions
                    modify column installment_plan_status enum('WAITING','SETTLED','ON_TIME','OVERDUE','PAID','LATE','NOT_REALIZED','PARTIALLY_REALIZED') not null
                    """);

            jdbcTemplate.execute("""
                    update financial_transactions ft
                    join installment_plan_transactions plan
                      on plan.financial_transaction_id = ft.id
                    set ft.status = case
                        when plan.installment_plan_status = 'PAID' then 'SETTLED'
                        when plan.installment_plan_status in ('LATE', 'NOT_REALIZED', 'OVERDUE') then 'OVERDUE'
                        when plan.installment_plan_status = 'ON_TIME' then 'ON_TIME'
                        else 'WAITING'
                    end
                    """);

            jdbcTemplate.execute("""
                    alter table installment_plan_transactions
                    drop column installment_plan_status
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

            jdbcTemplate.execute(String.format("""
                    update %s
                    set status = case
                        when status = 'PAID' then 'SETTLED'
                        when status = 'NOT_REALIZED' then 'CANCELED'
                        else 'WAITING'
                    end
                    where status not in ('SETTLED', 'WAITING', 'CANCELED')
                    """, tableName));

            jdbcTemplate.execute(String.format("""
                    alter table %s
                    modify column status enum('SETTLED','WAITING','CANCELED') not null
                    """, tableName));
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

        removeLegacyFinancialTransactionBookingLink();

        jdbcTemplate.execute("""
                update financial_transactions transaction
                join bookings booking
                  on transaction.description = concat('Pagamento da reserva #', booking.id)
                set transaction.source_type = 'BOOKING',
                    transaction.source_id = booking.id
                where transaction.source_type is null
                """);
    }

    void removeLegacyFinancialTransactionBookingLink() {
        if (columnExists("financial_transactions", "booking_id")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set source_type = 'BOOKING',
                        source_id = booking_id
                    where source_type is null
                      and source_id is null
                      and booking_id is not null
                    """);

            List<String> constraintNameList = foreignKeysForColumn(
                    "financial_transactions",
                    "booking_id"
            );
            for (String constraintName : constraintNameList) {
                jdbcTemplate.execute("alter table `financial_transactions` drop foreign key "
                        + sqlIdentifier(constraintName));
            }

            jdbcTemplate.execute("alter table `financial_transactions` drop column booking_id");
        }
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

    void migrateFinancialTransactionClassification() {
        if (!tableExists("financial_transactions")) {
            return;
        }

        if (columnExists("financial_transactions", "amount")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set amount = abs(amount)
                    """);
        }

        dropColumnIfExists("financial_transactions", "entry_amount");
        dropColumnIfExists("financial_transactions", "expense_amount");

        if (!columnExists("financial_transactions", "type")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column type varchar(50) not null default 'STANDARD'
                    """);
        }

        jdbcTemplate.execute("""
                alter table financial_transactions
                modify column type varchar(50) null
                """);
        jdbcTemplate.execute("""
                update financial_transactions
                set type = 'PLAN_DOWN_PAYMENT'
                where type = 'PLAN_SIGNAL_TRANSACTIONAL'
                """);
        jdbcTemplate.execute("""
                update financial_transactions
                set type = 'PLAN_TRANSACTION'
                where type = 'PLAN_TRANSACTIONAL'
                """);
        if (tableExists("installment_plan_transactions")) {
            jdbcTemplate.execute("""
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
        }
        if (tableExists("installment_transactions")) {
            jdbcTemplate.execute("""
                    update financial_transactions
                    set type = 'INSTALLMENT_TRANSACTION'
                    where id in (
                        select financial_transaction_id
                        from installment_transactions
                    )
                    """);
            if (columnExists("financial_transactions", "source_type")
                    && columnExists("financial_transactions", "source_id")) {
                jdbcTemplate.execute("""
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
            }
        }
        jdbcTemplate.execute("""
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
        jdbcTemplate.execute("""
                alter table financial_transactions
                modify column type varchar(50) not null default 'STANDARD'
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

    void ensureCashierMovementTemporalColumns() {
        ensureCashierMovementTemporalColumns("cashier_entries", "entry_date");
        ensureCashierMovementTemporalColumns("cashier_expenses", "expense_date");
    }

    void ensureFinancialTransactionPlanSchema() {
        jdbcTemplate.execute("""
                create table if not exists financial_transaction_plans (
                    id bigint not null auto_increment,
                    sender_type enum('CASHIER','GUEST') not null,
                    sender_id bigint not null,
                    receiver_type enum('CASHIER','GUEST') not null,
                    receiver_id bigint not null,
                    source_type enum(
                        'MANUAL','BOOKING','STAY','CHECK_IN','CHECK_OUT','GUEST'
                    ) not null,
                    source_id bigint not null,
                    total_amount decimal(19,2) not null,
                    status enum(
                        'ACTIVE','PARTIALLY_SETTLED','OVERDUE','SETTLED','CANCELED'
                    ) not null,
                    plan_due_date date not null,
                    plan_settlement_date date null,
                    description varchar(500) not null,
                    version bigint not null default 0,
                    created_at datetime(6) not null,
                    updated_at datetime(6) not null,
                    primary key (id)
                )
                """);
        if (!indexExists(
                "financial_transaction_plans",
                "idx_financial_transaction_plan_source"
        )) {
            jdbcTemplate.execute("""
                    create index idx_financial_transaction_plan_source
                    on financial_transaction_plans (source_type, source_id)
                    """);
        }
        if (!tableExists("financial_transactions")) {
            return;
        }

        jdbcTemplate.execute("""
                alter table financial_transactions
                modify column source_type enum(
                    'MANUAL','BOOKING','STAY','CHECK_IN','CHECK_OUT',
                    'PLAN','INSTALLMENT','GUEST'
                ) null
                """);
        if (!columnExists("financial_transactions", "plan_component_order")) {
            jdbcTemplate.execute("""
                    alter table financial_transactions
                    add column plan_component_order int null
                    """);
        }
        if (!indexExists(
                "financial_transactions",
                "idx_financial_transaction_plan_membership"
        )) {
            jdbcTemplate.execute("""
                    create index idx_financial_transaction_plan_membership
                    on financial_transactions (
                        source_type,
                        source_id,
                        due_date,
                        plan_component_order
                    )
                    """);
        }
    }

    void ensureFinancialCommandIdempotencySchema() {
        jdbcTemplate.execute("""
                create table if not exists financial_command_idempotency (
                    id bigint not null auto_increment,
                    operation varchar(60) not null,
                    actor_reference varchar(180) not null,
                    idempotency_key varchar(120) not null,
                    status varchar(30) not null,
                    booking_id bigint null,
                    plan_id bigint null,
                    financial_transaction_id bigint null,
                    created_at datetime(6) not null,
                    completed_at datetime(6) null,
                    primary key (id),
                    constraint uk_financial_command_idempotency_scope unique (
                        operation,
                        actor_reference,
                        idempotency_key
                    )
                )
                """);
        if (!columnExists(
                "financial_command_idempotency",
                "financial_transaction_id"
        )) {
            jdbcTemplate.execute("""
                    alter table financial_command_idempotency
                    add column financial_transaction_id bigint null
                    """);
        }
        if (!indexExists(
                "financial_command_idempotency",
                "idx_financial_command_idempotency_outcome"
        )) {
            jdbcTemplate.execute("""
                    create index idx_financial_command_idempotency_outcome
                    on financial_command_idempotency (
                        booking_id,
                        plan_id,
                        financial_transaction_id
                    )
                    """);
        }
    }

    void ensureNotifierSchema() {
        jdbcTemplate.execute("""
                create table if not exists notification_intents (
                    id char(36) not null,
                    source_system varchar(100) not null,
                    external_event_id varchar(160) not null,
                    idempotency_key varchar(200) not null,
                    correlation_key varchar(200) null,
                    notification_type varchar(100) not null,
                    channel varchar(30) not null,
                    delivery_profile_key varchar(100) not null,
                    recipient varchar(320) null,
                    subject varchar(255) null,
                    text_body longtext null,
                    html_body longtext null,
                    status varchar(40) not null,
                    attempt_count int not null,
                    next_attempt_at datetime(6) null,
                    lease_until datetime(6) null,
                    provider_message_id varchar(255) null,
                    last_error_category varchar(40) null,
                    created_at datetime(6) not null,
                    updated_at datetime(6) not null,
                    accepted_at datetime(6) null,
                    delivered_at datetime(6) null,
                    failed_at datetime(6) null,
                    retention_until datetime(6) not null,
                    version bigint not null default 0,
                    primary key (id),
                    constraint uk_notification_intent_source_idempotency unique (
                        source_system,
                        idempotency_key
                    ),
                    constraint uk_notification_intent_provider_message unique (
                        provider_message_id
                    )
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists notification_provider_events (
                    id char(36) not null,
                    notification_intent_id char(36) not null,
                    transport_event_id varchar(255) not null,
                    provider_event_id varchar(255) null,
                    provider_message_id varchar(255) not null,
                    event_type varchar(40) not null,
                    bounce_type varchar(100) null,
                    bounce_sub_type varchar(100) null,
                    provider_status_code varchar(100) null,
                    failure_category varchar(40) null,
                    occurred_at datetime(6) not null,
                    received_at datetime(6) not null,
                    processed_at datetime(6) null,
                    raw_event_storage_key varchar(512) null,
                    primary key (id),
                    constraint uk_notification_provider_event_transport unique (
                        transport_event_id
                    ),
                    constraint uk_notification_provider_event_provider unique (
                        provider_event_id
                    ),
                    constraint fk_notification_provider_event_intent foreign key (
                        notification_intent_id
                    ) references notification_intents(id)
                )
                """);
        ensureNotifierIndex(
                "notification_intents",
                "idx_notification_intent_dispatch",
                "status, next_attempt_at, lease_until"
        );
        ensureNotifierIndex(
                "notification_intents",
                "idx_notification_intent_retention",
                "retention_until"
        );
        ensureNotifierIndex(
                "notification_provider_events",
                "idx_notification_provider_event_intent",
                "notification_intent_id"
        );
        ensureNotifierIndex(
                "notification_provider_events",
                "idx_notification_provider_event_message",
                "provider_message_id"
        );
        if (!indexExists(
                "notification_intents",
                "uk_notification_intent_source_idempotency"
        )) {
            jdbcTemplate.execute("""
                    create unique index uk_notification_intent_source_idempotency
                    on notification_intents (source_system, idempotency_key)
                    """);
        }
        if (!indexExists(
                "notification_intents",
                "uk_notification_intent_provider_message"
        )) {
            jdbcTemplate.execute("""
                    create unique index uk_notification_intent_provider_message
                    on notification_intents (provider_message_id)
                    """);
        }
        if (!indexExists(
                "notification_provider_events",
                "uk_notification_provider_event_transport"
        )) {
            jdbcTemplate.execute("""
                    create unique index uk_notification_provider_event_transport
                    on notification_provider_events (transport_event_id)
                    """);
        }
        if (!indexExists(
                "notification_provider_events",
                "uk_notification_provider_event_provider"
        )) {
            jdbcTemplate.execute("""
                    create unique index uk_notification_provider_event_provider
                    on notification_provider_events (provider_event_id)
                    """);
        }
        if (!foreignKeyExists(
                "notification_provider_events",
                "notification_intent_id"
        )) {
            jdbcTemplate.execute("""
                    alter table notification_provider_events
                    add constraint fk_notification_provider_event_intent
                    foreign key (notification_intent_id)
                    references notification_intents(id)
                    """);
        }
    }

    private void ensureNotifierIndex(
            String tableName,
            String indexName,
            String columnExpression
    ) {
        if (!indexExists(tableName, indexName)) {
            jdbcTemplate.execute(String.format(
                    "create index %s on %s (%s)",
                    indexName,
                    tableName,
                    columnExpression
            ));
        }
    }

    private void ensureCashierMovementTemporalColumns(
            String tableName,
            String legacyDateColumnName
    ) {
        if (!tableExists(tableName)) {
            return;
        }
        if (!columnExists(tableName, "due_date")) {
            jdbcTemplate.execute(String.format("""
                    alter table %s
                    add column due_date date null
                    """, tableName));
        }
        if (columnExists(tableName, legacyDateColumnName)) {
            jdbcTemplate.execute(String.format("""
                    update %s
                    set due_date = %s
                    where due_date is null
                    """, tableName, legacyDateColumnName));
            dropColumnIfExists(tableName, legacyDateColumnName);
        }
        if (!columnExists(tableName, "settlement_date")) {
            jdbcTemplate.execute(String.format("""
                    alter table %s
                    add column settlement_date date null
                    """, tableName));
        }
        jdbcTemplate.execute(String.format("""
                alter table %s
                modify column due_date date not null
                """, tableName));
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

    private boolean indexExists(String tableName, String indexName) {
        Integer indexes = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.statistics
                where table_schema = database()
                  and table_name = ?
                  and index_name = ?
                """, Integer.class, tableName, indexName);

        return indexes != null && indexes > 0;
    }

    private boolean foreignKeyExists(String tableName, String columnName) {
        Integer constraints = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.key_column_usage
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                  and referenced_table_name is not null
                """, Integer.class, tableName, columnName);

        return constraints != null && constraints > 0;
    }

    private boolean isMysql(Connection connection) throws Exception {
        return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql");
    }
}
