package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public class WebIpAuthenticationDetails extends WebAuthenticationDetails {
    private final String ipAddress;

    public WebIpAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.ipAddress = extractIpAddress(request);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    private static String extractIpAddress(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        } else {
            return request.getRemoteAddr();
        }
    }
}
