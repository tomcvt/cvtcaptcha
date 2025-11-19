package com.tomcvt.cvtcaptcha.exceptions;

public class CaptchaLimitExceededException extends RuntimeException {
    public CaptchaLimitExceededException(String message) {
        super(message);
    }
    
}
