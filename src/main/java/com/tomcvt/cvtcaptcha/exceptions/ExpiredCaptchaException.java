package com.tomcvt.cvtcaptcha.exceptions;

public class ExpiredCaptchaException extends RuntimeException {
    public ExpiredCaptchaException(String message) {
        super(message);
    }
    
}
