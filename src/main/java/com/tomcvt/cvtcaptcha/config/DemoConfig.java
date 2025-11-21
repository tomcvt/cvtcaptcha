package com.tomcvt.cvtcaptcha.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.tomcvt.cvtcaptcha.zdemo.DemoUsers;

@Configuration
@Profile({"demo", "dev"})
public class DemoConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DemoConfig.class);
    private final DemoUsers demoUsers;

    public DemoConfig(DemoUsers demoUsers) {
        this.demoUsers = demoUsers;
    }

    @Bean
    public CommandLineRunner demoDataLoader() {
        return args -> {
            log.info("Loading demo data...");
            demoUsers.createDemoUsers();
            log.info("Demo data loaded.");
        };
    }
}
