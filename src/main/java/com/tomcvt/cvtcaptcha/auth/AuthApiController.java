package com.tomcvt.cvtcaptcha.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.dtos.LoginRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthService authService;
    
    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(request.username(), request.password());
    }
}
