package com.tomcvt.cvtcaptcha.service;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HmacHashService {
    private final SecretKeySpec secretKeySpec;


    public HmacHashService(@Value("${com.tomcvt.hmac-hash-service.secret}") String secret) {
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
    }

    public String hash(String data) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC hash", e);
        }
    }

    public boolean verifyHash(String data, String hash) {
        String computedHash = hash(data);
        return safeEquals(computedHash, hash);
    }

    private boolean safeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
