package com.tomcvt.cvtcaptcha.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

public record ApiKeyRequest(
    String username,
    @NotNull @Length(min = 3, max = 100) String domainUrl,
    @NotNull @Length(min = 3, max = 50)  String name
) {
    
}
