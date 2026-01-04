package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tomcvt.cvtcaptcha.auth.AuthService;


@Controller
public class RegistrationController {
    private final AuthService authService;

    public RegistrationController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/activate-account")
    public String activateAccount(@RequestParam String token) {
        boolean activated = authService.activateAccount(token);
        if (activated) {
            return "login?activated";
        }
        return "login?activationError";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }
}
