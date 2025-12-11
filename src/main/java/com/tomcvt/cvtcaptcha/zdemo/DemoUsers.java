package com.tomcvt.cvtcaptcha.zdemo;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.auth.AuthService;

@Component
@Profile({"demo","dev"})
public class DemoUsers {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DemoUsers.class);
    private final AuthService authService;
    private final Environment env;

    public DemoUsers(AuthService authService, Environment env) {
        this.authService = authService;
        this.env = env;
    }

    public void createDemoUsers() {
        if (env.acceptsProfiles(Profiles.of("demo"))) {
            log.info("Demo profile active - not creating dev users");
            return;
        }
        //authService.registerActivatedUser("anonymous", "", "", "ANON");
        //authService.registerActivatedUser("admin", "123", "abc@gmail.com", "ADMIN");
        //authService.registerActivatedUser("user", "123", "abe@gmail.com", "USER");
    }
}
