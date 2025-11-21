package com.tomcvt.cvtcaptcha.zdemo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.auth.AuthService;

@Component
@Profile({"demo", "dev"})
public class DemoUsers {
    private final AuthService authService;

    public DemoUsers(AuthService authService) {
        this.authService = authService;
    }

    public void createDemoUsers() {
        authService.registerActivatedUser("admin", "123", "abc@gmail.com", "ADMIN");
        authService.registerActivatedUser("user", "123", "abe@gmail.com", "USER");
    }
}
