package com.tomcvt.cvtcaptcha.exceptions;

import org.springframework.security.core.AuthenticationException;

public class ExpiredApiKeyException extends AuthenticationException {
    public ExpiredApiKeyException(String msg) {
        super(msg);
    }
    
}
