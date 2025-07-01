package com.tezish.demo.exception;


import com.tezish.demo.dto.response.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage()); // Get translated message
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(HttpStatus.BAD_REQUEST, errors.toString()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            Exception.class,
            BadRequestException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<?> HandleGenericException(Exception e) {
        log.info("GenericException: {}", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({
            SecurityException.class
    })
    public ResponseEntity<?> HandleSecurityException(Exception e) {
        log.info("SecurityException: {}", e);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(HttpStatus.FORBIDDEN, e.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
            UsernameNotFoundException.class,
            AuthenticationException.class
    })
    public ResponseEntity<?> UnauthorizedException(Exception e) {
        log.info("UnauthorizedException: {}", e);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }
}
