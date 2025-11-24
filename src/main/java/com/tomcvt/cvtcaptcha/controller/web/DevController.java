package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.network.AnonRequestLimiter;

@Controller
@RequestMapping("/dev")
@Profile({"dev", "demo"})
public class DevController {
    private final AnonRequestLimiter rateLimiter;

    public DevController(AnonRequestLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping({ "","/"})
    public String home(@AuthenticationPrincipal SecureUserDetails userDetails, Model model) {
        model.addAttribute("userIp", userDetails.getIp());
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("remainingRequests", rateLimiter.getRemainingRequests(userDetails.getIp()));
        return "dev/testing-home";
    }
}
