package com.tomcvt.cvtcaptcha.network;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IpRegistryCleaner {
    private final AnonRequestLimiter anonRequestLimiter;
    private boolean enabled = true;

    public IpRegistryCleaner(AnonRequestLimiter anonRequestLimiter) {
        this.anonRequestLimiter = anonRequestLimiter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Scheduled(fixedRateString = "${com.tomcvt.request.ip-registry-cleaner.interval-ms}")
    public void cleanIpRegistry() {
        if (!enabled) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        anonRequestLimiter.requestCounts.entrySet().removeIf(entry -> 
            currentTimeMillis - entry.getValue().getLastRequestMillis() > anonRequestLimiter.resetMilillis
        );
    }
}
