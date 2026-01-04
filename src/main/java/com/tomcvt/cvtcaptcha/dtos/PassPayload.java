package com.tomcvt.cvtcaptcha.dtos;

public record PassPayload(
    String oldPassword,
    String newPassword
) {
    
}
