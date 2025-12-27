package com.tomcvt.cvtcaptcha.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.dtos.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final AuthService authService;
    
    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        String xff = httpRequest.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            clientIp = xff.split(",")[0].trim();
        }
        return authService.authenticate(request.username(), request.password(), clientIp);
    }
}
