package com.househost.audit.adapter.out.config;

import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(200)
public class AuditProcessingOperationBackfillInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    public AuditProcessingOperationBackfillInitializer(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        associate(DataProcessingOperationCodes.BOOKING_MANAGEMENT, "PUBLIC_BOOKING_CREATED", "PRIVACY_ACCEPTED");
        associate(DataProcessingOperationCodes.WHATSAPP_MARKETING, "MARKETING_OPT_IN_ACCEPTED");
        reassociate(
                DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT,
                "USER_LOGIN_FAILED",
                "USER_LOGIN_BLOCKED",
                "USER_LOGIN_RATE_LIMITED",
                "LOGIN_PROTECTION_UNAVAILABLE"
        );
    }

    private void associate(String operationCode, String... eventTypes) {
        String placeholders = String.join(",", java.util.Collections.nCopies(eventTypes.length, "?"));
        Object[] parameters = new Object[eventTypes.length + 1];
        parameters[0] = operationCode;
        System.arraycopy(eventTypes, 0, parameters, 1, eventTypes.length);
        jdbcTemplate.update("""
                update audit_events audit
                join data_processing_operations operation on operation.operation_code = ?
                set audit.processing_operation_id = operation.id
                where audit.processing_operation_id is null
                  and audit.event_type in (%s)
                """.formatted(placeholders), parameters);
    }

    private void reassociate(String operationCode, String... eventTypes) {
        String placeholders = String.join(",", java.util.Collections.nCopies(eventTypes.length, "?"));
        Object[] parameters = new Object[eventTypes.length + 1];
        parameters[0] = operationCode;
        System.arraycopy(eventTypes, 0, parameters, 1, eventTypes.length);
        jdbcTemplate.update("""
                update audit_events audit
                join data_processing_operations operation on operation.operation_code = ?
                set audit.processing_operation_id = operation.id
                where audit.event_type in (%s)
                  and (audit.processing_operation_id is null
                       or audit.processing_operation_id <> operation.id)
                """.formatted(placeholders), parameters);
    }
}
