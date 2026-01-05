package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER', 'TOMCVT')")
@Controller
@RequestMapping("/user")
public class UserController {
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "user/key-management";
    }

}
