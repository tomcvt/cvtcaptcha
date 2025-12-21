package com.tomcvt.cvtcaptcha.config;

import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties.Logging;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.auth.AnonUserAuthenticationFilter;
import com.tomcvt.cvtcaptcha.logging.LoggingFilterRegistry;

@Service
public class AppReadyInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final SuperUserInitializer superUserInitializer;
    private final AnonUserInitializer anonUserInitializer;
    private final AnonUserAuthenticationFilter anonUserAuthenticationFilter;
    private final LoggingFilterRegistry loggingFilterRegistry;

    public AppReadyInitializer(SuperUserInitializer superUserInitializer,
            AnonUserInitializer anonUserInitializer,
            AnonUserAuthenticationFilter anonUserAuthenticationFilter,
            LoggingFilterRegistry loggingFilterRegistry
            ) {
        this.superUserInitializer = superUserInitializer;
        this.anonUserInitializer = anonUserInitializer;
        this.anonUserAuthenticationFilter = anonUserAuthenticationFilter;
        this.loggingFilterRegistry = loggingFilterRegistry;
    
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        anonUserInitializer.init();
        superUserInitializer.init();
        anonUserAuthenticationFilter.setAnonUser(anonUserInitializer.getAnonUser());
        //loggingFilterRegistry.registerFilters();
    }

}
