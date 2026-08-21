package com.househost.observability.domain.exception;

public class ClientLogRateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public ClientLogRateLimitExceededException(long retryAfterSeconds) {
        super("Limite de logs do cliente excedido.");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
