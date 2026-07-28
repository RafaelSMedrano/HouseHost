package com.househost.shared.exception;

import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyUnavailableException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.shared.dto.ResponseDTO;
import com.househost.supplier.domain.exception.SupplierException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import com.househost.security.domain.exception.SecurityAccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, SecurityAccessDeniedException.class})
    public ResponseEntity<ResponseDTO> handleAccessDenied(RuntimeException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ResponseDTO> handleInvalidLogin(InvalidLoginException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LoginTemporarilyBlockedException.class)
    public ResponseEntity<ResponseDTO> handleLoginTemporarilyBlocked(LoginTemporarilyBlockedException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(LoginProtectionUnavailableException.class)
    public ResponseEntity<ResponseDTO> handleLoginProtectionUnavailable(LoginProtectionUnavailableException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<ResponseDTO> handleRegistration(RegistrationException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(GuestException.class)
    public ResponseEntity<ResponseDTO> handleGuest(GuestException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RoomException.class)
    public ResponseEntity<ResponseDTO> handleRoom(RoomException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<ResponseDTO> handleBooking(BookingException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(FinanceException.class)
    public ResponseEntity<ResponseDTO> handleFinance(FinanceException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PrivacyException.class)
    public ResponseEntity<ResponseDTO> handlePrivacy(PrivacyException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PrivacyPolicyUnavailableException.class)
    public ResponseEntity<ResponseDTO> handlePrivacyPolicyUnavailable(
            PrivacyPolicyUnavailableException exception
    ) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(PrivacyPolicyConflictException.class)
    public ResponseEntity<ResponseDTO> handlePrivacyPolicyConflict(
            PrivacyPolicyConflictException exception
    ) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SupplierException.class)
    public ResponseEntity<ResponseDTO> handleSupplier(SupplierException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
