package com.househost.auth.application.service;

import com.househost.auth.application.config.LoginSecurityPolicyProperties;
import com.househost.auth.application.config.LoginSecurityPolicyProperties.ScopePolicy;
import com.househost.auth.application.records.ActiveLoginRestrictionRecord;
import com.househost.auth.application.records.LoginSecurityFailureResultRecord;
import com.househost.auth.application.port.out.LoginSecurityKeyPort;
import com.househost.auth.application.port.out.LoginSecurityPersistencePort;
import com.househost.auth.domain.model.LoginSecurityControl;
import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LoginSecurityService {
    private final LoginSecurityPersistencePort persistencePort;
    private final LoginSecurityKeyPort keyPort;
    private final LoginSecurityPolicyProperties properties;
    private final Clock clock;

    public LoginSecurityService(LoginSecurityPersistencePort persistencePort, LoginSecurityKeyPort keyPort,
                               LoginSecurityPolicyProperties properties, Clock clock) {
        this.persistencePort = persistencePort;
        this.keyPort = keyPort;
        this.properties = properties;
        this.clock = clock;
    }

    public Optional<ActiveLoginRestrictionRecord> ensureAllowed(String email, String ipAddress) {
        Instant now = clock.instant();
        return scopeKeys(email, ipAddress).stream()
                .map(key -> persistencePort.find(key.scope(), key.key()))
                .flatMap(Optional::stream)
                .filter(control -> control.isBlocked(now))
                .map(control -> new ActiveLoginRestrictionRecord(control.getScope(), control.getBlockedUntil(),
                        control.remainingSeconds(now)))
                .max(Comparator.comparingLong(ActiveLoginRestrictionRecord::remainingSeconds));
    }

    public List<LoginSecurityFailureResultRecord> registerFailure(String email, String ipAddress) {
        Instant now = clock.instant();
        List<LoginSecurityFailureResultRecord> updates = new ArrayList<>();
        for (ScopeKey scopeKey : scopeKeys(email, ipAddress)) {
            ScopePolicy policy = policy(scopeKey.scope());
            boolean[] newlyBlocked = {false};
            LoginSecurityControl control = persistencePort.mutate(scopeKey.scope(), scopeKey.key(), current ->
                    newlyBlocked[0] = current.registerFailure(now, policy.getMaxFailures(),
                            policy.getWindow(), policy.getBlock()));
            updates.add(new LoginSecurityFailureResultRecord(control.getScope(), control.getFailureCount(),
                    newlyBlocked[0], control.getBlockedUntil()));
        }
        return List.copyOf(updates);
    }

    public void registerSuccess(String email, String ipAddress) {
        Instant now = clock.instant();
        String normalizedEmail = normalizeEmail(email);
        String normalizedIp = normalizeIp(ipAddress);
        persistencePort.clear(LoginSecurityScope.EMAIL_IP, keyPort.forPair(normalizedEmail, normalizedIp), now);
        persistencePort.clear(LoginSecurityScope.ACCOUNT, keyPort.forEmail(normalizedEmail), now);
    }

    public int purgeExpiredState() {
        Instant now = clock.instant();
        return persistencePort.deleteStale(now.minus(properties.getRetention()), now);
    }

    public String deriveEmailHmacKey(String email) {
        return keyPort.forEmail(normalizeEmail(email));
    }

    private List<ScopeKey> scopeKeys(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedIp = normalizeIp(ipAddress);
        return List.of(
                new ScopeKey(LoginSecurityScope.EMAIL_IP, keyPort.forPair(normalizedEmail, normalizedIp)),
                new ScopeKey(LoginSecurityScope.IP, keyPort.forIp(normalizedIp)),
                new ScopeKey(LoginSecurityScope.ACCOUNT, keyPort.forEmail(normalizedEmail)));
    }

    private ScopePolicy policy(LoginSecurityScope scope) {
        return switch (scope) {
            case EMAIL_IP -> properties.getPair();
            case IP -> properties.getIp();
            case ACCOUNT -> properties.getAccount();
        };
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) throw new IllegalArgumentException("IP address is required");
        return ipAddress.trim().toLowerCase(Locale.ROOT);
    }

    private record ScopeKey(LoginSecurityScope scope, String key) { }
}
