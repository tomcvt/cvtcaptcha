package com.tomcvt.cvtcaptcha.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.CaptchaLimitExceededException;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.network.SimpleRequestCounter;


// Service to handle internal (site owned) captcha creation limiting, mainly for demo purposes so strictly limited.
@Service
public class InternalUserLimiter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalUserLimiter.class);
    private final Map<Long, SimpleRequestCounter> userRequestCounters = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long timeWindowMs;

    public InternalUserLimiter(
        @Value("${com.tomcvt.captcha.user-internal-api.max-requests}") int maxRequests,
        @Value("${com.tomcvt.captcha.user-internal-api.time-window-ms}") long timeWindowMs
    ) {
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeWindowMs;
    }

    public void checkAndIncrementUserCaptchaLimit(User user) {
        Long userId = user.getId();
        long currentTimeMillis = System.currentTimeMillis();
        userRequestCounters.compute(userId, (id, counter) -> {
            if (counter == null) {
                counter = new SimpleRequestCounter();
                return counter;
            }
            counter.increment();
            if (currentTimeMillis - counter.getWindowStartMillis() > timeWindowMs) {
                counter.resetCounter();
                return counter;
            }
            if (counter.getCount() > maxRequests) {
                throw new CaptchaLimitExceededException("User captcha limit exceeded for this method, use API key with higher limits.");
            }
            return counter;
        });
        log.debug("Internal user {} is not subject to captcha limits.", user.getUsername());
    }

    @Scheduled(fixedRate = 600000) // Clean up every 10 minutes
    public void cleanupOldEntries() {
        long currentTimeMillis = System.currentTimeMillis();
        userRequestCounters.entrySet().removeIf(entry -> 
            currentTimeMillis - entry.getValue().getWindowStartMillis() > timeWindowMs
        );
    }
}
