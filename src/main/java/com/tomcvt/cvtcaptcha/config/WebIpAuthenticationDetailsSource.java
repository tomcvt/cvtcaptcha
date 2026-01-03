package com.tomcvt.cvtcaptcha.config;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.auth.WebIpAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class WebIpAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebIpAuthenticationDetails> {

    @Override
    public WebIpAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new WebIpAuthenticationDetails(context);
    }
    
}
