package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(@AuthenticationPrincipal SecureUserDetails userDetails, Model model) {
        model.addAttribute("userIp", userDetails.getIp());
        return "home";
    }
}
