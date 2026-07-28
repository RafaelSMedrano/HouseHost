package com.househost.auth.adapter.out.integration;

import com.househost.auth.application.records.LoginSecurityAlertMessageRecord;
import com.househost.auth.application.port.out.LoginSecurityAlertPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OperationalLogLoginSecurityAlertAdapter implements LoginSecurityAlertPort {
    private static final Logger SECURITY_ALERT = LoggerFactory.getLogger("HOUSEHOST_SECURITY_ALERT");
    private final String destination;

    public OperationalLogLoginSecurityAlertAdapter(
            @Value("${househost.login-protection.alert-destination}") String destination) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("A monitored login security alert destination is required");
        }
        this.destination = destination.trim();
    }

    @Override
    public void send(LoginSecurityAlertMessageRecord alert) {
        SECURITY_ALERT.warn("destination={} type={} scope={} count={} blockedUntil={} emailHmacKey={} detail={}",
                destination, alert.type(), alert.scope(), alert.failureCount(), alert.blockedUntil(),
                alert.emailHmacKey(), alert.detail());
    }
}
