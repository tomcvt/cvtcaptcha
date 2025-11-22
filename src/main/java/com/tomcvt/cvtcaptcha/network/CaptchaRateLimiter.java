package com.tomcvt.cvtcaptcha.network;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.CaptchaLimitExceededException;

@Service
public class CaptchaRateLimiter {
    private final ConcurrentHashMap<String, RequestCounter> captchaRequestsCounter = new ConcurrentHashMap<>();
    private final long resetMilillis;
    private final int maxRequests;

    public CaptchaRateLimiter(@Value("${com.tomcvt.captcha.anon-rate-limit.time-window-ms}") long resetMilillis,
                       @Value("${com.tomcvt.captcha.anon-rate-limit.max-requests}") int maxRequests) {
        this.resetMilillis = resetMilillis;
        this.maxRequests = maxRequests;
    }

    public void checkAndIncrementAnonymousLimit(String ip) throws CaptchaLimitExceededException {
        long currentTimeMillis = System.currentTimeMillis();
        captchaRequestsCounter.compute(ip, (k, counter) -> {
            if (counter == null) {
                return new RequestCounter(currentTimeMillis);
            } else {
                if (currentTimeMillis - counter.getWindowStartMillis() > resetMilillis) {
                    counter.reset(currentTimeMillis);
                } else {
                    counter.increment();
                }

                if (counter.getCount() > maxRequests) {
                    throw new CaptchaLimitExceededException("Captcha rate limit exceeded for IP: " + ip);
                }
                
                return counter;
            }
        });
    }
}
