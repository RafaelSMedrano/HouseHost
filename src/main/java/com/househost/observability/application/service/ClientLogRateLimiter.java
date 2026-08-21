package com.househost.observability.application.service;

import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.domain.exception.ClientLogRateLimitExceededException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientLogRateLimiter {

    private final int actorMaximum;
    private final int originMaximum;
    private final int maximumKeys;
    private final Duration window;
    private final Clock clock;
    private final Map<String, WindowCounter> counterByKeyMap = new ConcurrentHashMap<>();

    @Autowired
    public ClientLogRateLimiter(
            @Value("${househost.client-logging.rate-limit.actor-max:60}") int actorMaximum,
            @Value("${househost.client-logging.rate-limit.origin-max:120}") int originMaximum,
            @Value("${househost.client-logging.rate-limit.max-keys:2000}") int maximumKeys,
            @Value("${househost.client-logging.rate-limit.window:PT1M}") Duration window
    ) {
        this(actorMaximum, originMaximum, maximumKeys, window, Clock.systemUTC());
    }

    ClientLogRateLimiter(int actorMaximum, int originMaximum, int maximumKeys, Duration window, Clock clock) {
        if (actorMaximum < 1 || originMaximum < 1 || maximumKeys < 2 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Invalid client log rate-limit configuration");
        }
        this.actorMaximum = actorMaximum;
        this.originMaximum = originMaximum;
        this.maximumKeys = maximumKeys;
        this.window = window;
        this.clock = clock;
    }

    public synchronized void verify(ClientLogRequestContextRecord contextRecord) {
        Instant now = clock.instant();
        removeExpired(now);
        increment("actor:" + contextRecord.actorReference(), actorMaximum, now);
        increment("origin:" + contextRecord.originReference(), originMaximum, now);
    }

    private void increment(String key, int maximum, Instant now) {
        WindowCounter counter = counterByKeyMap.get(key);
        if (counter == null) {
            if (counterByKeyMap.size() >= maximumKeys) {
                throw new ClientLogRateLimitExceededException(window.toSeconds());
            }
            counterByKeyMap.put(key, new WindowCounter(now.plus(window), 1));
            return;
        }
        if (counter.count() >= maximum) {
            throw new ClientLogRateLimitExceededException(Duration.between(now, counter.expiresAt()).toSeconds());
        }
        counterByKeyMap.put(key, new WindowCounter(counter.expiresAt(), counter.count() + 1));
    }

    private void removeExpired(Instant now) {
        counterByKeyMap.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private record WindowCounter(Instant expiresAt, int count) {
    }
}
