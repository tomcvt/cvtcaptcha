package com.tomcvt.cvtcaptcha.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetailsService;
import com.tomcvt.cvtcaptcha.auth.WebIpAuthenticationDetails;

@Configuration
public class AuthConfig {
    private final PasswordEncoder passwordEncoder;


    public AuthConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean 
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(SecureUserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
