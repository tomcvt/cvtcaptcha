package com.tomcvt.cvtcaptcha.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.network.AnonRequestLimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AnonUserAuthenticationFilter extends OncePerRequestFilter {
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnonUserAuthenticationFilter.class);
    private final AnonRequestLimiter rateLimiter;
    private User anonUser;

    public AnonUserAuthenticationFilter(AnonRequestLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public void setAnonUser(User anonUser) {
        this.anonUser = anonUser;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String xff = request.getHeader("X-Forwarded-For");
            String ipAddress;
            if (xff != null && !xff.isEmpty()) {
            ipAddress = request.getHeader("X-Forwarded-For").split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }
            SecureUserDetails anonUserDetails = new SecureUserDetails(false, anonUser, ipAddress);
            var authToken = new UsernamePasswordAuthenticationToken(
                    anonUserDetails, null, anonUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("Authenticated anonymous user with IP: " + ipAddress);
            try {
                //TODO change to anon rate limiter
                rateLimiter.checkRateLimitAndIncrement(ipAddress);
            } catch (Exception e) {
                response.sendError(429, "Rate limit exceeded");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    
}
