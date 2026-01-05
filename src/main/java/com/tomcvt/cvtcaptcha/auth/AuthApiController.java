package com.tomcvt.cvtcaptcha.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.dtos.EmailInput;
import com.tomcvt.cvtcaptcha.dtos.LoginRequest;
import com.tomcvt.cvtcaptcha.dtos.PasswordChangeInput;
import com.tomcvt.cvtcaptcha.dtos.RegisterRequest;
import com.tomcvt.cvtcaptcha.dtos.TextResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthService authService;
    
    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        String xff = httpRequest.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            clientIp = xff.split(",")[0].trim();
        }
        return ResponseEntity.ok(authService.authenticate(request.username(), request.password(), clientIp));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody EmailInput emailInput) {
        authService.initiatePasswordRecovery(emailInput.email());
        return ResponseEntity.ok(new TextResponse("If the email exists in our system, a password recovery link has been sent."));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordChangeInput input) {
        authService.resetPasswordWithToken(input.token(), input.newPassword(), input.confirmPassword());
        return ResponseEntity.ok(new TextResponse("Password has been reset successfully"));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.registerUserWithEmail(request.username(), request.rawPassword(), request.email(), "USER");
        return ResponseEntity.ok(new TextResponse("Registration successful. Please check your email to activate your account."));
    }

    
}
