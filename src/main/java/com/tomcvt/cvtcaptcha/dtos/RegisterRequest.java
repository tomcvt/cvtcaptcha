package com.tomcvt.cvtcaptcha.dtos;

public record RegisterRequest(
    String username,
    String rawPassword,
    String email
) {
    
}
