package com.househost.observability.domain.exception;

public class ClientLogRejectedException extends RuntimeException {

    public ClientLogRejectedException() {
        super("Log do cliente invalido.");
    }
}
