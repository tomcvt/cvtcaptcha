package com.tomcvt.cvtcaptcha.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginTracker {
    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private final Map<String, LoginCounter> loginAttempts = new ConcurrentHashMap<>();

    
    public boolean recordLoginAttemptForIp(String username , String clientIp) {
        loginAttempts.compute(clientIp, (key, counter) -> {
            if (counter == null) {
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
}
