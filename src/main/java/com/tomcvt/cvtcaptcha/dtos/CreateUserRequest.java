package com.tomcvt.cvtcaptcha.dtos;

public record CreateUserRequest(
    String username,
    String password,
    String email,
    String role
) {
    
}
