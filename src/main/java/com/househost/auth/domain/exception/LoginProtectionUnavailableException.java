package com.househost.auth.domain.exception;

public class LoginProtectionUnavailableException extends RuntimeException {
    public LoginProtectionUnavailableException() {
        super("Servico de autenticacao temporariamente indisponivel. Tente novamente mais tarde.");
    }

    public LoginProtectionUnavailableException(Throwable cause) {
        super("Servico de autenticacao temporariamente indisponivel. Tente novamente mais tarde.", cause);
    }
}
