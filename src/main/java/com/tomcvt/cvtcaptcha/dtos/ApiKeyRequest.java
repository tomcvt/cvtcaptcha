package com.tomcvt.cvtcaptcha.dtos;

public record ApiKeyRequest(
    String username,
    String domainUrl,
    String name
) {
    
}
