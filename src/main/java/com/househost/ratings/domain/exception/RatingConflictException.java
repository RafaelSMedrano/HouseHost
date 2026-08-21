package com.househost.ratings.domain.exception;

public class RatingConflictException extends RatingException {

    public RatingConflictException(String message) {
        super(message);
    }

    public RatingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
