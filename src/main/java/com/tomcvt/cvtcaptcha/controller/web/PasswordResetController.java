package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {
    @GetMapping("/public/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }
    @GetMapping("/public/recover-password")
    public String showRecoverPasswordForm() {
        return "auth/recover-password";
    }
}
