package com.househost.notifier.adapter.in.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SnsSesFeedbackController.class)
@ConditionalOnProperty(
        name = "househost.notifier.sns.enabled",
        havingValue = "true"
)
public class SnsFeedbackExceptionHandler {

    @ExceptionHandler(SnsFeedbackException.class)
    public ResponseEntity<Void> handle(SnsFeedbackException snsFeedbackException) {
        return ResponseEntity.status(snsFeedbackException.getHttpStatus()).build();
    }
}
