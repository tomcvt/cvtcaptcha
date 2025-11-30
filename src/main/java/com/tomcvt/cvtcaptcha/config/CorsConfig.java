package com.tomcvt.cvtcaptcha.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    private final String allowedOriginsString;

    public CorsConfig(@Value("${cors.allowed-origins}") String allowedOriginsString) {
        this.allowedOriginsString = allowedOriginsString;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = List.of(allowedOriginsString.split(","));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 1) Internal frontend (cookies allowed)
        CorsConfiguration internal = new CorsConfiguration();
        allowedOrigins.forEach(internal::addAllowedOrigin);
        internal.addAllowedMethod("*");
        internal.addAllowedHeader("*");
        internal.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/internal/**", internal);
        source.registerCorsConfiguration("/api/auth/**", internal);
        source.registerCorsConfiguration("/login", internal);

        // 2) Public client CAPTCHA usage (no cookies)
        CorsConfiguration publicConf = new CorsConfiguration();
        publicConf.addAllowedOriginPattern("*"); 
        publicConf.addAllowedMethod("*");
        publicConf.addAllowedHeader("*");
        publicConf.setAllowCredentials(false); 
        source.registerCorsConfiguration("/api/captcha/**", publicConf);

        return source;
    }
}
