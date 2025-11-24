package com.tomcvt.cvtcaptcha.auth;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DisabledLoginRegistry {
    private final Map<String, String> disabledLogins;

    public DisabledLoginRegistry() {
        this.disabledLogins = Map.of(
            "anon", "Anonymous login is disabled.",
            "anonymous", "Anonymous login is disabled."
        );
    }

    public boolean isLoginDisabled(String username) {
        return disabledLogins.containsKey(username.toLowerCase());
    }
}
