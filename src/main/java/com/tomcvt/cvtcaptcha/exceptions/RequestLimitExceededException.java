package com.tomcvt.cvtcaptcha.exceptions;

public class RequestLimitExceededException extends RuntimeException {
    public RequestLimitExceededException(String message) {
        super(message);
    }
}
