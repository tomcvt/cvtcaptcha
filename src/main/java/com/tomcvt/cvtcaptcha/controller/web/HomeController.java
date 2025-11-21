package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.network.GlobalRateLimiter;

@Controller
public class HomeController {
    private final GlobalRateLimiter rateLimiter;

    public HomeController(GlobalRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal SecureUserDetails userDetails, Model model) {
        model.addAttribute("userIp", userDetails.getIp());
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("remainingRequests", rateLimiter.getRemainingRequests(userDetails.getIp()));
        return "home";
    }
}
