package com.tomcvt.cvtcaptcha.dtos;

public record ApiKeyDetailsDto(
    String apiKey,
    String name,
    String domainUrl,
    String version,
    Integer remainingRequests
) {
    
}
