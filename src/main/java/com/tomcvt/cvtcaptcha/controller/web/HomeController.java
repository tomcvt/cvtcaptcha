package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.network.AnonRequestLimiter;

@Controller
public class HomeController {
    private final AnonRequestLimiter rateLimiter;

    public HomeController(AnonRequestLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal SecureUserDetails userDetails, Model model) {
        model.addAttribute("userIp", userDetails.getIp());
        model.addAttribute("username", userDetails.getUsername());
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANON"))) {
            model.addAttribute("isAnon", true);
        } else {
            model.addAttribute("isAnon", false);
        }
        model.addAttribute("remainingRequests", rateLimiter.getRemainingRequests(userDetails.getIp()));
        return "home";
    }
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
