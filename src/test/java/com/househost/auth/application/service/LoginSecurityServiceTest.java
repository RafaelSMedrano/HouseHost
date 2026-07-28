package com.househost.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;

import com.househost.auth.adapter.out.security.HmacLoginSecurityKeyAdapter;
import com.househost.auth.application.config.LoginSecurityPolicyProperties;
import com.househost.auth.application.port.out.LoginSecurityPersistencePort;
import com.househost.auth.domain.model.LoginSecurityControl;
import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class LoginSecurityServiceTest {
    private static final Instant NOW = Instant.parse("2026-07-24T12:00:00Z");

    @Test
    void concurrentFailuresReachTenAndBlockPair() throws Exception {
        InMemoryPersistence persistence = new InMemoryPersistence();
        LoginSecurityService service = service(persistence);
        try (var executor = Executors.newFixedThreadPool(10)) {
            CountDownLatch start = new CountDownLatch(1);
            List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    service.registerFailure("Admin@Example.com", "203.0.113.10");
                    return null;
                }));
            }
            start.countDown();
            for (var future : futures) future.get();
        }
        var restriction = service.ensureAllowed("admin@example.com", "203.0.113.10").orElseThrow();
        assertEquals(LoginSecurityScope.EMAIL_IP, restriction.scope());
        assertEquals(10, persistence.byScope(LoginSecurityScope.EMAIL_IP).getFailureCount());
    }

    @Test
    void ipAndAccountThresholdsAndSuccessResetHaveSpecifiedBehavior() {
        InMemoryPersistence persistence = new InMemoryPersistence();
        LoginSecurityService service = service(persistence);
        for (int i = 0; i < 30; i++) service.registerFailure("person" + i + "@example.com", "198.51.100.7");
        assertTrue(persistence.byScope(LoginSecurityScope.IP).isBlocked(NOW));

        InMemoryPersistence distributed = new InMemoryPersistence();
        service = service(distributed);
        for (int i = 0; i < 20; i++) service.registerFailure("target@example.com", "198.51.100." + i);
        assertTrue(distributed.byScope(LoginSecurityScope.ACCOUNT).isBlocked(NOW));
        service.registerSuccess("target@example.com", "198.51.100.0");
        assertEquals(0, distributed.byScope(LoginSecurityScope.ACCOUNT).getFailureCount());
        assertEquals(1, distributed.byScope(LoginSecurityScope.IP).getFailureCount());
    }

    private LoginSecurityService service(InMemoryPersistence persistence) {
        return new LoginSecurityService(persistence, new HmacLoginSecurityKeyAdapter("test-secret"),
                new LoginSecurityPolicyProperties(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class InMemoryPersistence implements LoginSecurityPersistencePort {
        private final Map<String, LoginSecurityControl> values = new HashMap<>();
        @Override public synchronized Optional<LoginSecurityControl> find(LoginSecurityScope scope, String key) {
            return Optional.ofNullable(values.get(scope + ":" + key));
        }
        @Override public synchronized LoginSecurityControl mutate(LoginSecurityScope scope, String key,
                                                                 Consumer<LoginSecurityControl> mutation) {
            LoginSecurityControl control = values.computeIfAbsent(scope + ":" + key,
                    ignored -> new LoginSecurityControl(scope, key));
            mutation.accept(control);
            return control;
        }
        @Override public synchronized void clear(LoginSecurityScope scope, String key, Instant now) {
            Optional.ofNullable(values.get(scope + ":" + key)).ifPresent(value -> value.clear(now));
        }
        @Override public int deleteStale(Instant cutoff, Instant now) { return 0; }
        synchronized LoginSecurityControl byScope(LoginSecurityScope scope) {
            return values.values().stream().filter(value -> value.getScope() == scope).findFirst().orElseThrow();
        }
    }
}
