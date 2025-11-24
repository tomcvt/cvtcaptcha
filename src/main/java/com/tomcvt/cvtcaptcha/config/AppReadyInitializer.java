package com.tomcvt.cvtcaptcha.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.auth.AnonUserAuthenticationFilter;

@Service
public class AppReadyInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final SuperUserInitializer superUserInitializer;
    private final AnonUserInitializer anonUserInitializer;
    private final AnonUserAuthenticationFilter anonUserAuthenticationFilter;

    public AppReadyInitializer(SuperUserInitializer superUserInitializer,
            AnonUserInitializer anonUserInitializer,
            AnonUserAuthenticationFilter anonUserAuthenticationFilter
            ) {
        this.superUserInitializer = superUserInitializer;
        this.anonUserInitializer = anonUserInitializer;
        this.anonUserAuthenticationFilter = anonUserAuthenticationFilter;
    
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        anonUserInitializer.init();
        superUserInitializer.init();
        anonUserAuthenticationFilter.setAnonUser(anonUserInitializer.getAnonUser());
    }

}
