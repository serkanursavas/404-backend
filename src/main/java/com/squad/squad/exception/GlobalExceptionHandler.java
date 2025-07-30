package com.squad.squad.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    /**
     * Handle group access violations
     */
    @ExceptionHandler(GroupAccessException.class)
    public ResponseEntity<ExceptionResponse> handleGroupAccessException(GroupAccessException ex, WebRequest request) {
        logger.warn("Group access violation: {} - User Group: {}, Requested Group: {}", 
                ex.getMessage(), ex.getUserGroupId(), ex.getRequestedGroupId());
        
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Erişim reddedildi: Bu grup verilerine erişim yetkiniz yok.",
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle tenant context issues
     */
    @ExceptionHandler(TenantContextException.class)
    public ResponseEntity<ExceptionResponse> handleTenantContextException(TenantContextException ex, WebRequest request) {
        logger.error("Tenant context error: {} - Expected: {}, Actual: {}", 
                ex.getMessage(), ex.getExpectedTenantId(), ex.getActualTenantId());
        
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Sistem hatası: Grup bağlamı sorunu. Lütfen tekrar giriş yapın.",
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle Spring Security access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {} for request: {}", ex.getMessage(), request.getDescription(false));
        
        // Check if it's a group-related access denial
        String message = ex.getMessage();
        String userFriendlyMessage;
        
        if (message != null && message.contains("Group")) {
            if (message.contains("Group membership required")) {
                userFriendlyMessage = "Bu işlem için grup üyeliği gereklidir.";
            } else if (message.contains("Group admin role required")) {
                userFriendlyMessage = "Bu işlem için grup yöneticisi yetkisi gereklidir.";
            } else if (message.contains("pending state")) {
                userFriendlyMessage = "Grup onayınız bekleniyor. Bu işlemi yapma yetkiniz henüz yok.";
            } else {
                userFriendlyMessage = "Bu gruba erişim yetkiniz yok.";
            }
        } else {
            userFriendlyMessage = "Bu işlemi gerçekleştirme yetkiniz yok.";
        }
        
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                userFriendlyMessage,
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Path variable hatası olduğunda 400 Bad Request döndüren hata yöneticisi
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Invalid path variable type: " + ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Endpoint bulunamadığında 404 Not Found döndüren hata yöneticisi
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Endpoint not found: " + ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGlobalException(Exception ex, WebRequest request) {
        ExceptionResponse response = new ExceptionResponse(
                LocalDateTime.now(),
                "Internal Server Error: " + ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}