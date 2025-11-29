package com.tomcvt.cvtcaptcha.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.exceptions.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ExpiredCaptchaTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCaptchaTokenException(ExpiredCaptchaTokenException ex) {
        log.warn("ExpiredCaptchaTokenException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ErrorResponse("EXPIRED_TOKEN", ex.getMessage())
        );
    }
    @ExceptionHandler(CaptchaLimitExceededException.class)
    public ResponseEntity<String> handleCaptchaLimitExceededException(CaptchaLimitExceededException ex) {
        log.warn("CaptchaLimitExceededException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }
    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<String> handleRequestLimitExceededException(RequestLimitExceededException ex) {
        log.warn("RequestLimitExceededException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }
    @ExceptionHandler(WrongTypeException.class)
    public ResponseEntity<String> handleWrongTypeException(WrongTypeException ex) {
        log.warn("WrongTypeException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("InvalidTokenException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ErrorResponse("INVALID_TOKEN", ex.getMessage())
        );
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("NoResourceFoundException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(ExpiredCaptchaException.class)
    public ResponseEntity<String> handleExpiredCaptchaException(ExpiredCaptchaException ex) {
        log.warn("ExpiredCaptchaException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<String> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.warn("AuthorizationDeniedException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization denied");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred.")
        );
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HttpRequestMethodNotSupportedException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage())
        );
    }
}
