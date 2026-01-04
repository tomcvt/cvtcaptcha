package com.tomcvt.cvtcaptcha.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final SecureUserDetailsService secureUserDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, 
            SecureUserDetailsService secureUserDetailsService) {
        this.jwtService = jwtService;
        this.secureUserDetailsService = secureUserDetailsService;
    }

    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");

        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null && request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        //JwtAuthentication overwrites api key authentication

        if (username != null) {
            SecureUserDetails userDetails = (SecureUserDetails) secureUserDetailsService.loadUserByUsername(username);
            String xff = request.getHeader("X-Forwarded-For");
            String ipAddress;
            if (xff != null && !xff.isEmpty()) {
            ipAddress = request.getHeader("X-Forwarded-For").split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }
            userDetails.setIp(ipAddress);
            if (jwtService.validateToken(token, userDetails)) {
                //TODO consider adding ip address logging, create a custom AuthenticationToken
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            log.info("Authenticated with jwt user: " + username + " with IP: " + ipAddress);
        }

        filterChain.doFilter(request, response);
    }
}
