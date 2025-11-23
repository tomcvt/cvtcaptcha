package com.tomcvt.cvtcaptcha.dtos;

public record ConsumerApiKeyResponse(
    String apiKey,
    String username,
    String domainUrl,
    String name
) {
    
}
