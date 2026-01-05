package com.tomcvt.cvtcaptcha.exceptions;

public class WrongAuthenticationMethodException extends RuntimeException {
    public WrongAuthenticationMethodException(String message) {
        super(message);
    }
    
}
