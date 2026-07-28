package com.househost.auth.domain.exception;

public class LoginTemporarilyBlockedException extends RuntimeException {
    private final long retryAfterSeconds;

    public LoginTemporarilyBlockedException(long retryAfterSeconds) {
        super("Muitas tentativas de acesso. Aguarde alguns minutos e tente novamente.");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long getRetryAfterSeconds() { return retryAfterSeconds; }
}
