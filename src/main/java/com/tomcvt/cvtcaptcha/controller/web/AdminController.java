package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPERUSER')")
public class AdminController {
    @GetMapping("/dashboard")
    public String getAdminDashboard(@AuthenticationPrincipal SecureUserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        return "admin/admin-dashboard";
    }
    @GetMapping("/logging")
    public String getLoggingDashboard() {
        return "admin/logging-dashboard";
    }
    
}
