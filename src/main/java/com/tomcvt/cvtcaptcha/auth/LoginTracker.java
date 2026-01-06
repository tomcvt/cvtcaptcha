package com.tomcvt.cvtcaptcha.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LoginTracker {
    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private final Map<String, LoginCounter> loginAttempts = new ConcurrentHashMap<>();
    private static final long RESET_WINDOW_MILLIS = 15 * 60 * 1000; // 15 minutes

    
    public boolean recordLoginAttemptForIp(String username , String clientIp) {
        loginAttempts.compute(clientIp, (key, counter) -> {
            if (counter == null) {
                return new LoginCounter();
            } 
            if (System.currentTimeMillis() - counter.getWindowStartMillis() > RESET_WINDOW_MILLIS) {
                return new LoginCounter();
            } else {
                counter.increment();
                return counter;
            }
        });
        LoginCounter counter = loginAttempts.get(clientIp);
        if (counter.getCount() > MAX_LOGIN_ATTEMPTS) {
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // every 60 minutes
    public void resetLoginAttempts() {
        loginAttempts.entrySet().removeIf(
            entry -> entry.getValue().getWindowStartMillis() + RESET_WINDOW_MILLIS < System.currentTimeMillis()
        );
    }
}
