package com.tomcvt.cvtcaptcha.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.repository.ConsumerApiKeyRepository;

@Service
public class ApiKeyRegistry {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiKeyRegistry.class);
    private final ConsumerApiKeyRepository consumerApiKeyRepository;
    private final Map<String, ConsumerApiKeyData> apiKeyCache;

    public ApiKeyRegistry(ConsumerApiKeyRepository consumerApiKeyRepository) {
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.apiKeyCache = new ConcurrentHashMap<>();
    }

    public void registerApiKeyHash(String apiKeyHash, ConsumerApiKeyData details) {
        var existing = apiKeyCache.get(apiKeyHash);
        if (existing != null) {
            log.error("FATAL: Attempt to register an already registered API Key Hash: {}", apiKeyHash);
            throw new IllegalArgumentException("API Key already registered");
        }
        apiKeyCache.put(apiKeyHash, details);
    }

    public ConsumerApiKeyData getApiKeyData(String apiKeyHash) {
        return apiKeyCache.computeIfAbsent(apiKeyHash, hash -> 
            consumerApiKeyRepository.findByApiKeyHash(hash).orElse(null)
        );
    }

    public void invalidateApiKeyCache(String apiKeyHash) {
        apiKeyCache.remove(apiKeyHash);
    }

    public ConsumerApiKeyData refreshApiKeyData(String apiKeyHash) {
        ConsumerApiKeyData apiKeyData = consumerApiKeyRepository.findByApiKeyHash(apiKeyHash).orElse(null);
        if (apiKeyData != null) {
            apiKeyCache.put(apiKeyHash, apiKeyData);
        } else {
            apiKeyCache.remove(apiKeyHash);
        }
        return apiKeyData;
    }


}
