package com.tomcvt.cvtcaptcha.network;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.RateLimitExceededException;

@Service
public class AnonRequestLimiter {
    protected final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    protected final long resetMilillis;
    private final int maxRequests;

    public AnonRequestLimiter(@Value("${com.tomcvt.request.anon-rate-limit.time-window-ms}") long resetMilillis, 
                       @Value("${com.tomcvt.request.anon-rate-limit.max-requests}") int maxRequests) {
        this.resetMilillis = resetMilillis;
        this.maxRequests = maxRequests;
    }

    public RequestCounter getRequestCounter(String key) {
        return requestCounts.get(key);
    }



    public int getRemainingRequests(String key) {
        RequestCounter counter = requestCounts.get(key);
        if (counter == null) {
            return maxRequests;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - counter.getWindowStartMillis() > resetMilillis) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - counter.getCount());
    }

    public void checkRateLimitAndIncrement(String key) throws RateLimitExceededException {
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
