package com.househost.observability.domain.exception;

public class ClientLogUnavailableException extends RuntimeException {

    public ClientLogUnavailableException() {
        super("Recebimento de log temporariamente indisponivel.");
    }
}
