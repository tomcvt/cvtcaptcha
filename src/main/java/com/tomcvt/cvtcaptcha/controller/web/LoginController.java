package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.tomcvt.cvtcaptcha.auth.AuthService;
import com.tomcvt.cvtcaptcha.auth.DisabledLoginRegistry;
import com.tomcvt.cvtcaptcha.auth.JwtResponse;
import com.tomcvt.cvtcaptcha.dtos.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    private final AuthService authService;
    private final DisabledLoginRegistry disabledLoginRegistry;

    LoginController(AuthService authService, DisabledLoginRegistry disabledLoginRegistry) {
        this.authService = authService;
        this.disabledLoginRegistry = disabledLoginRegistry;
    }
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request,
                                HttpServletResponse response
    ) {
        if (disabledLoginRegistry.isLoginDisabled(loginRequest.username())) {
            throw new IllegalArgumentException("This login method is disabled for this user");
        }
        String clientIp = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            clientIp = xff.split(",")[0].trim();
        }
        JwtResponse jwtResponse = authService.authenticate(loginRequest.username(), loginRequest.password(), clientIp);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwtResponse.token())
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        // TODO: better redirect
        return "redirect:/";
    }
    @PostMapping("/logout")
    public String processLogout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return "redirect:/login?logout";
    }
    
}
