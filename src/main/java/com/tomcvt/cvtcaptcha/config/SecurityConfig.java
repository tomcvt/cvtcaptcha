package com.tomcvt.cvtcaptcha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tomcvt.cvtcaptcha.auth.AnonUserAuthenticationFilter;
import com.tomcvt.cvtcaptcha.auth.ApiKeyAuthenticationFilter;
import com.tomcvt.cvtcaptcha.auth.DefaultAccessDeniedHandler;
import com.tomcvt.cvtcaptcha.auth.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AnonUserAuthenticationFilter anonUserAuthenticationFilter;
    private final String[] WHITELIST = {
        "/api/auth/**",
        "/error",
        "/public/**",
        "/css/**",
        "/js/**",
        "/images/**",
    };

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter, 
            AnonUserAuthenticationFilter anonUserAuthenticationFilter, ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.anonUserAuthenticationFilter = anonUserAuthenticationFilter;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            //TODO learn where the actual authorization HttpRequest is happening (probably context aware filter)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(WHITELIST).permitAll()
                .anyRequest().authenticated()
            )
            .authenticationManager(authenticationManager)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(anonUserAuthenticationFilter, AnonymousAuthenticationFilter.class)
            .addFilterBefore(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class)
            .anonymous(anonymous -> anonymous.disable())
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(new DefaultAccessDeniedHandler()));

        return http.build();
    }
}
