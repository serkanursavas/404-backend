package com.squad.squad.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialsException(InvalidCredentialsException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Diğer genel hatalar için
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGlobalException(Exception ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Internal Server Error: " + ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}