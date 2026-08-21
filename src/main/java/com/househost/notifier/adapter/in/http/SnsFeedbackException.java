package com.househost.notifier.adapter.in.http;

import org.springframework.http.HttpStatus;

public class SnsFeedbackException extends RuntimeException {

    private final HttpStatus httpStatus;

    private SnsFeedbackException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public static SnsFeedbackException malformed(String message, Throwable cause) {
        return new SnsFeedbackException(HttpStatus.BAD_REQUEST, message, cause);
    }

    public static SnsFeedbackException forbidden(String message, Throwable cause) {
        return new SnsFeedbackException(HttpStatus.FORBIDDEN, message, cause);
    }

    public static SnsFeedbackException unavailable(String message, Throwable cause) {
        return new SnsFeedbackException(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
