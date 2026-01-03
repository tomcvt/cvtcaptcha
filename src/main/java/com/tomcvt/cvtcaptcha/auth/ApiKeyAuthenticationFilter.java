package com.tomcvt.cvtcaptcha.auth;

import java.io.IOException;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tomcvt.cvtcaptcha.config.WebIpAuthenticationDetailsSource;
import com.tomcvt.cvtcaptcha.service.ConsumerApiKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//TODO add logging to security files, monitor failed attempts for ip
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private final ConsumerApiKeyService consumerApiKeyService;

    public ApiKeyAuthenticationFilter(ConsumerApiKeyService consumerApiKeyService) {
        this.consumerApiKeyService = consumerApiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey != null) {
            String xff = request.getHeader("X-Forwarded-For");
            String ipAddress;
            if (xff != null && !xff.isEmpty()) {
                ipAddress = xff.split(",")[0].trim();
            } else {
                ipAddress = request.getRemoteAddr();
            }

            CachedUserDetails userDetails = null;
            try {
                userDetails = consumerApiKeyService.authenticate(apiKey);
            } catch (AuthenticationException e) {
                log.warn("Failed to authenticate API key: " + e.getMessage() + " from IP: " + ipAddress);
                //TODO consider handling ip blocking on multiple failed attempts
                writeErrorResponse(response, e.getMessage());
                return;
            }
            // TODO consider adding ip address logging, create a custom AuthenticationToken
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebIpAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("Authenticated API key for user: {}, with IP: {}", userDetails.getUsername(), ipAddress);
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }

}
