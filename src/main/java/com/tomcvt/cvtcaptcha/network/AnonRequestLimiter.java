package com.tomcvt.cvtcaptcha.network;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.IllegalUsageException;
import com.tomcvt.cvtcaptcha.exceptions.RateLimitExceededException;

@Service
public class AnonRequestLimiter {
    protected final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    protected final long resetMilillis;
    private final int maxRequests;
    private final int banRatePerMinute;

    public AnonRequestLimiter(@Value("${com.tomcvt.request.anon-rate-limit.time-window-ms}") long resetMilillis, 
                       @Value("${com.tomcvt.request.anon-rate-limit.max-requests}") int maxRequests, 
                       @Value("${com.tomcvt.request.anon-rate-limit.ban-rate-per-minute}") int banRatePerMinute,
                       BanRegistry banRegistry) {
        this.resetMilillis = resetMilillis;
        this.maxRequests = maxRequests;
        this.banRatePerMinute = banRatePerMinute;
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

    public void checkRateLimitAndIncrement(String key) throws RateLimitExceededException, IllegalUsageException {
        long currentTimeMillis = System.currentTimeMillis();
        requestCounts.compute(key, (k, counter) -> {
            if (counter == null) {
                return new RequestCounter();
            } else {
                if (currentTimeMillis - counter.getWindowStartMillis() > resetMilillis) {
                    counter.resetCounter();
                }
                if (currentTimeMillis - counter.getBanWindowStartMillis() > 60000) {
                    counter.resetBanCounter();
                }
                counter.increment();
                return counter;
            }
        });

        RequestCounter counter = requestCounts.get(key);
        if (counter.getBanCount() > banRatePerMinute) {
            throw new IllegalUsageException("Excessive requests detected for key: " + key);
        }
        if (counter.getCount() > maxRequests) {
            throw new RateLimitExceededException("Rate limit exceeded for key: " + key);
        }
    }
}
