package com.househost.shared.exception;

import com.househost.shared.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ResponseDTO> handleInvalidLogin(InvalidLoginException exception) {
        ResponseDTO response = new ResponseDTO("error", exception.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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
}
