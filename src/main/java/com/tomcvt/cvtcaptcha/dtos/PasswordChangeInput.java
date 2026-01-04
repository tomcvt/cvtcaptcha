package com.tomcvt.cvtcaptcha.dtos;

public record PasswordChangeInput(
    String token,
    String newPassword,
    String confirmPassword
) {
    
}
