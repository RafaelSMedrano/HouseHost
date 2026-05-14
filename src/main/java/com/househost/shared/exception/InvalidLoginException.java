package com.househost.shared.exception;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException() {
        super("Usuario ou senha invalidos");
    }
}
