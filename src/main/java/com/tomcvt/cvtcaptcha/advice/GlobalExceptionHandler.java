package com.tomcvt.cvtcaptcha.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.exceptions.*;

import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static SimpleGrantedAuthority anonAuthority = new SimpleGrantedAuthority("ROLE_ANON");
    @ExceptionHandler(ExpiredCaptchaTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCaptchaTokenException(ExpiredCaptchaTokenException ex) {
        log.warn("ExpiredCaptchaTokenException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("EXPIRED_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(CaptchaLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleCaptchaLimitExceededException(CaptchaLimitExceededException ex) {
        log.warn("CaptchaLimitExceededException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                new ErrorResponse("CAPTCHA_LIMIT_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRequestLimitExceededException(RequestLimitExceededException ex) {
        log.warn("RequestLimitExceededException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                new ErrorResponse("REQUEST_LIMIT_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(WrongTypeException.class)
    public ResponseEntity<ErrorResponse> handleWrongTypeException(WrongTypeException ex) {
        log.warn("WrongTypeException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse("WRONG_TYPE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("InvalidTokenException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("INVALID_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("NoResourceFoundException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse("NO_RESOURCE_FOUND", "Resource not found"));
    }

    @ExceptionHandler(ExpiredCaptchaException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCaptchaException(ExpiredCaptchaException ex) {
        log.warn("ExpiredCaptchaException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(
                new ErrorResponse("EXPIRED_CAPTCHA", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse("ILLEGAL_ARGUMENT", ex.getMessage()));
    }/*
      * @ExceptionHandler(AuthorizationDeniedException.class)
      * public ResponseEntity<ErrorResponse>
      * handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
      * log.warn("AuthorizationDeniedException: " + ex.getMessage());
      * return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      * new ErrorResponse("AUTHORIZATION_DENIED", "Authorization denied")
      * );
      * }
      */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("HttpRequestMethodNotSupportedException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                new ErrorResponse("METHOD_NOT_ALLOWED", ex.getMessage()));
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ErrorResponse> handleRequestRejectedException(RequestRejectedException ex) {
        log.warn("RequestRejectedException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse("REQUEST_REJECTED", ex.getMessage()));
    }

    @ExceptionHandler(IllegalUsageException.class)
    public ResponseEntity<ErrorResponse> handleIllegalUsageException(IllegalUsageException ex) {
        log.warn("IllegalUsageException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                new ErrorResponse("ILLEGAL_USAGE", ex.getMessage()));
    }

    @ExceptionHandler(WrongAuthenticationMethodException.class)
    public ResponseEntity<ErrorResponse> handleWrongAuthenticationMethodException(
            WrongAuthenticationMethodException ex) {
        log.warn("WrongAuthenticationMethodException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse("WRONG_AUTH_METHOD", "Authenticate using standard method (browser)"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDeniedException(AuthorizationDeniedException ex,
        HttpServletResponse response
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getAuthorities().contains(anonAuthority)) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", "/login");
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse("FORBIDDEN", "You do not have permission to perform this action")
                );
            }
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/login");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }
}