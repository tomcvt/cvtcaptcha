package com.tomcvt.cvtcaptcha.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidApiKeyException extends AuthenticationException {
    public InvalidApiKeyException(String msg) {
        super(msg);
    }
}
