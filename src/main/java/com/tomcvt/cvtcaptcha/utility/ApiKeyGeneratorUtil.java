package com.tomcvt.cvtcaptcha.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGeneratorUtil {
    private final SecureRandom secureRandom;
    private final Base64.Encoder encoder;

    public ApiKeyGeneratorUtil() {
        this.secureRandom = new SecureRandom();
        this.encoder = Base64.getUrlEncoder().withoutPadding();
    }

    public String generateApiKey() {
        byte[] apiKeyBytes = new byte[32];
        secureRandom.nextBytes(apiKeyBytes);
        String rawApiKey = encoder.encodeToString(apiKeyBytes);
        return rawApiKey;
    }
}
