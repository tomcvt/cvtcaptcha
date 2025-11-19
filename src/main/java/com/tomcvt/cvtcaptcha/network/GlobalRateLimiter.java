package com.tomcvt.cvtcaptcha.network;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.RateLimitExceededException;

@Service
public class GlobalRateLimiter {
    private final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final long resetMilillis;
    private final int maxRequests;

    public GlobalRateLimiter(@Value("${com.tomcvt.cvtcaptcha.request.rate-limit.time-window-ms}") long resetMilillis, 
                       @Value("${com.tomcvt.cvtcaptcha.request.rate-limit.max-requests}") int maxRequests) {
        this.resetMilillis = resetMilillis;
        this.maxRequests = maxRequests;
    }

    public void checkRateLimit(String key) throws RateLimitExceededException {
        long currentTimeMillis = System.currentTimeMillis();
        requestCounts.compute(key, (k, counter) -> {
            if (counter == null) {
                return new RequestCounter(currentTimeMillis);
            } else {
                if (currentTimeMillis - counter.getWindowStartMillis() > resetMilillis) {
                    counter.reset(currentTimeMillis);
                } else {
                    counter.increment();
                }
                return counter;
            }
        });

        RequestCounter counter = requestCounts.get(key);
        if (counter.getCount() > maxRequests) {
            throw new RateLimitExceededException("Rate limit exceeded for key: " + key);
        }
    }
}
