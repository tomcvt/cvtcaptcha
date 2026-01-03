package com.tomcvt.cvtcaptcha.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.auth.CachedUserDetails;

@Service
public class ApiKeyCache {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiKeyCache.class);
    private final Map<String, CachedUserDetails> cache = new ConcurrentHashMap<>();
    
    public CachedUserDetails get(String apiKeyHash) {
        return cache.get(apiKeyHash);
    }

    public void put(String apiKeyHash, CachedUserDetails userDetails) {
        cache.put(apiKeyHash, userDetails);
        log.debug("Cached API key details for key hash: {}", apiKeyHash);
    }

    public void evict(String apiKeyHash) {
        cache.remove(apiKeyHash);
        log.debug("Evicted API key details for key hash: {}", apiKeyHash);
    }
}
