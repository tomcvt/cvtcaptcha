package com.tomcvt.cvtcaptcha.exceptions;

public class ExpiredCaptchaTokenException extends RuntimeException {
    public ExpiredCaptchaTokenException(String message) {
        super(message);
    }
}
