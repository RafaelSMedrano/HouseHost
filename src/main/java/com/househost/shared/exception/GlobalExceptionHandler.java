package com.househost.shared.exception;

import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import com.househost.finance.financialtransaction.domain.exception.FinancialTransactionPlanConflictException;
import com.househost.observability.domain.exception.ClientLogRateLimitExceededException;
import com.househost.observability.domain.exception.ClientLogRejectedException;
import com.househost.observability.domain.exception.ClientLogUnavailableException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyUnavailableException;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.exception.RatingEligibilityException;
import com.househost.ratings.domain.exception.RatingException;
import com.househost.security.domain.exception.SecurityAccessDeniedException;
import com.househost.shared.dto.ResponseDTO;
import com.househost.supplier.domain.exception.SupplierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNEXPECTED_ERROR_MESSAGE = "Nao foi possivel concluir a operacao.";

    @ExceptionHandler({AccessDeniedException.class, SecurityAccessDeniedException.class})
    public ResponseEntity<ResponseDTO> handleAccessDenied(RuntimeException exception) {
        logHandledException(exception, HttpStatus.FORBIDDEN);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ResponseDTO> handleInvalidLogin(InvalidLoginException exception) {
        logHandledException(exception, HttpStatus.UNAUTHORIZED);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LoginTemporarilyBlockedException.class)
    public ResponseEntity<ResponseDTO> handleLoginTemporarilyBlocked(LoginTemporarilyBlockedException exception) {
        logHandledException(exception, HttpStatus.TOO_MANY_REQUESTS);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(LoginProtectionUnavailableException.class)
    public ResponseEntity<ResponseDTO> handleLoginProtectionUnavailable(LoginProtectionUnavailableException exception) {
        logHandledException(exception, HttpStatus.SERVICE_UNAVAILABLE);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(ClientLogRejectedException.class)
    public ResponseEntity<ResponseDTO> handleClientLogRejected(ClientLogRejectedException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(new ResponseDTO("error", exception.getMessage(), null));
    }

    @ExceptionHandler(ClientLogRateLimitExceededException.class)
    public ResponseEntity<ResponseDTO> handleClientLogRateLimitExceeded(
            ClientLogRateLimitExceededException exception
    ) {
        logHandledException(exception, HttpStatus.TOO_MANY_REQUESTS);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.getRetryAfterSeconds()))
                .body(new ResponseDTO("error", exception.getMessage(), null));
    }

    @ExceptionHandler(ClientLogUnavailableException.class)
    public ResponseEntity<ResponseDTO> handleClientLogUnavailable(ClientLogUnavailableException exception) {
        logHandledException(exception, HttpStatus.SERVICE_UNAVAILABLE);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ResponseDTO("error", exception.getMessage(), null));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ResponseDTO> handleRegistration(RegistrationException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(GuestException.class)
    public ResponseEntity<ResponseDTO> handleGuest(GuestException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RoomException.class)
    public ResponseEntity<ResponseDTO> handleRoom(RoomException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ResponseDTO> handleBooking(BookingException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RatingConflictException.class)
    public ResponseEntity<ResponseDTO> handleRatingConflict(
            RatingConflictException exception
    ) {
        logHandledException(exception, HttpStatus.CONFLICT);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(RatingEligibilityException.class)
    public ResponseEntity<ResponseDTO> handleRatingEligibility(
            RatingEligibilityException exception
    ) {
        logHandledException(exception, HttpStatus.UNPROCESSABLE_ENTITY);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(RatingException.class)
    public ResponseEntity<ResponseDTO> handleRating(RatingException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FinanceException.class)
    public ResponseEntity<ResponseDTO> handleFinance(FinanceException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FinancialTransactionPlanConflictException.class)
    public ResponseEntity<ResponseDTO> handleFinancialTransactionPlanConflict(
            FinancialTransactionPlanConflictException exception
    ) {
        logHandledException(exception, HttpStatus.CONFLICT);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PrivacyException.class)
    public ResponseEntity<ResponseDTO> handlePrivacy(PrivacyException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PrivacyPolicyUnavailableException.class)
    public ResponseEntity<ResponseDTO> handlePrivacyPolicyUnavailable(
            PrivacyPolicyUnavailableException exception
    ) {
        logHandledException(exception, HttpStatus.SERVICE_UNAVAILABLE);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(PrivacyPolicyConflictException.class)
    public ResponseEntity<ResponseDTO> handlePrivacyPolicyConflict(
            PrivacyPolicyConflictException exception
    ) {
        logHandledException(exception, HttpStatus.CONFLICT);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SupplierException.class)
    public ResponseEntity<ResponseDTO> handleSupplier(SupplierException exception) {
        logHandledException(exception, HttpStatus.BAD_REQUEST);
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleUnexpected(Exception exception) {
        RuntimeException sanitizedException = new RuntimeException("Unexpected application failure");
        sanitizedException.setStackTrace(exception.getStackTrace());
        LOGGER.error(
                "event=exception.unhandled type={} status={}",
                exception.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                sanitizedException
        );
        ResponseDTO response = new ResponseDTO("error", UNEXPECTED_ERROR_MESSAGE, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        LOGGER.warn(
                "event=exception.handled type={} status={}",
                exception.getClass().getSimpleName(),
                statusCode.value()
        );
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }

    private void logHandledException(RuntimeException exception, HttpStatus status) {
        LOGGER.warn(
                "event=exception.handled type={} status={}",
                exception.getClass().getSimpleName(),
                status.value()
        );
    }
}
