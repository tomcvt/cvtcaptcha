package com.tomcvt.cvtcaptcha.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.auth.CachedUserDetails;
import com.tomcvt.cvtcaptcha.repository.UserLimitsRepository;

@Service
public class ApiKeyCache {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiKeyCache.class);
    private final Map<String, Long> idsCache = new ConcurrentHashMap<>();
    private final Map<Long, CachedUserDetails> detailsCache = new ConcurrentHashMap<>();
    private final UserLimitsRepository userLimitsRepository;

    public ApiKeyCache(UserLimitsRepository userLimitsRepository) {
        this.userLimitsRepository = userLimitsRepository;
    }
    
    public CachedUserDetails get(String apiKeyHash) {
        return detailsCache.get(idsCache.get(apiKeyHash));
    }

    public CachedUserDetails getByUserId(Long userId) {
        return detailsCache.get(userId);
    }

    public void put(String apiKeyHash, CachedUserDetails userDetails) {
        detailsCache.put(userDetails.getUserId(), userDetails);
        idsCache.put(apiKeyHash, userDetails.getUserId());
        log.debug("Cached API key details for key hash: {}", apiKeyHash);
    }

    public void evictUserDetailsAndHash(String apiKeyHash) {
        Long userId = idsCache.remove(apiKeyHash);
        if (userId != null) {
            detailsCache.remove(userId);
        }
        log.debug("Evicted API key details for key hash: {}", apiKeyHash);
    }

    public void evictHash(String apiKeyHash) {
        idsCache.remove(apiKeyHash);
        log.debug("Evicted API key hash: {}", apiKeyHash);
    }

    public void evictUserDetailsByUserId(Long userId) {
        detailsCache.remove(userId);
        log.debug("Evicted API key details for user ID: {}", userId);
    }

    //Every day on midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetCounters() {
        var iterator = detailsCache.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Long userId = entry.getKey();
            CachedUserDetails userDetails = entry.getValue();
            var limits = userLimitsRepository.findByUserId(userDetails.getUserId())
                .orElse(null);
            if (limits != null) {
                userDetails = CachedUserDetails.withRemainingRequests(userDetails, limits.getDailyCaptchaLimit());
                detailsCache.put(userId, userDetails);
            }
        }
        log.info("Reset daily request counters for all cached API keys.");
    }
}
