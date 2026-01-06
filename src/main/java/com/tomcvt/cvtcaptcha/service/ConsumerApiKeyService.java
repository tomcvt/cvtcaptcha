package com.tomcvt.cvtcaptcha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.auth.CachedUserDetails;
import com.tomcvt.cvtcaptcha.dtos.ApiKeyDetailsDto;
import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.dtos.UserLimitsInfo;
import com.tomcvt.cvtcaptcha.exceptions.ExpiredApiKeyException;
import com.tomcvt.cvtcaptcha.exceptions.InvalidApiKeyException;
import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;
import com.tomcvt.cvtcaptcha.repository.ConsumerApiKeyRepository;
import com.tomcvt.cvtcaptcha.repository.UserRepository;
import com.tomcvt.cvtcaptcha.utility.ApiKeyGeneratorUtil;

@Service
public class ConsumerApiKeyService {
    private final HmacHashService hmacHashService;
    private final ApiKeyGeneratorUtil apiKeyGeneratorUtil;
    private final ConsumerApiKeyRepository consumerApiKeyRepository;
    private final UserRepository userRepository;
    private final ApiKeyCache apiKeyCache;
    private final String currentVersion;
    private final UserLimitsService userLimitsService;

    public ConsumerApiKeyService(
            HmacHashService hmacHashService,
            ConsumerApiKeyRepository consumerApiKeyRepository,
            UserRepository userRepository,
            ApiKeyCache apiKeyCache,
            UserLimitsService userLimitsService,
            @Value("${com.tomcvt.hmac-hash-service.api-key-version}") String currentVersion) {
        this.hmacHashService = hmacHashService;
        this.apiKeyGeneratorUtil = new ApiKeyGeneratorUtil();
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.userRepository = userRepository;
        this.apiKeyCache = apiKeyCache;
        this.currentVersion = currentVersion;
        this.userLimitsService = userLimitsService;
    }

    // TODO cache api keys
    // TODO version control for api keys

    /**
     * Authenticates a user based on the provided API key.
     * <p>
     * This method attempts to retrieve user details from a cache using a hashed version of the API key.
     * If the details are not found in the cache, it queries the repository for the API key data.
     * The method also checks if the API key version matches the current version and throws an exception if it is outdated.
     * If authentication is successful, user details are cached and returned.
     * </p>
     *
     * @param apiKey the API key to authenticate
     * @return {@link CachedUserDetails} containing authenticated user information
     * @throws AuthenticationException if the API key is invalid or expired
     */
    public CachedUserDetails authenticate(String apiKey) throws AuthenticationException {
        String apiKeyHash = hmacHashService.hash(apiKey);
        var details = apiKeyCache.get(apiKeyHash);
        if (details != null) {
            if (!details.getApiKeyVersion().equals(currentVersion)) {
                throw new ExpiredApiKeyException("Outdated API Key, please create a new one");
            }
            return details;
        }
        ConsumerApiKeyData apiKeyData = consumerApiKeyRepository.findByApiKeyHash(apiKeyHash)
                .orElseThrow(() -> new InvalidApiKeyException("Invalid API Key"));
        String apiKeyVersion = apiKeyData.getApiKeyVersion();
        if (apiKeyVersion == null || !apiKeyVersion.equals(currentVersion)) {
            throw new ExpiredApiKeyException("Outdated API Key, please create a new one");
        }
        if (apiKeyData.isRevoked()) {
            throw new InvalidApiKeyException("API Key has been revoked");
        }
        // TODO make sure to use proper limits from user later, 500 for now
        User user = apiKeyData.getUser();
        var limits = userLimitsService.getLimitsForUser(user);
        Integer dailyLimit = limits.getDailyCaptchaLimit();
        details = CachedUserDetails.fromUser(user, dailyLimit, apiKeyVersion);
        apiKeyCache.put(apiKeyHash, details);
        return details;
    }

    public ConsumerApiKeyResponse validateAndGetConsumerApiKeyData(String apiKey) {
        String apiKeyHash = hmacHashService.hash(apiKey);
        return getConsumerApiKeyData(apiKeyHash);
    }

    public UserLimitsInfo getUserLimitsInfoByUser(User user) {
        var limits = userLimitsService.getLimitsForUser(user);
        var cachedDetails = apiKeyCache.getByUserId(user.getId());
        if (cachedDetails == null) {
            return new UserLimitsInfo(
                limits.getHourlyCaptchaLimit(),
                limits.getDailyCaptchaLimit(),
                "UNUSED"
            );
        }
        Integer dailyRemaining = cachedDetails.getRemainingRequests();
        String dailyRemainingStr = dailyRemaining != null ? dailyRemaining.toString() : "INFINITE";
        return new UserLimitsInfo(
            limits.getHourlyCaptchaLimit(),
            limits.getDailyCaptchaLimit(),
            dailyRemainingStr
        );
    }

    public ConsumerApiKeyResponse getConsumerApiKeyData(String apiKeyHash) {
        ConsumerApiKeyData apiKeyData = consumerApiKeyRepository.findByApiKeyHash(apiKeyHash)
                .orElseThrow(() -> new InvalidApiKeyException("Invalid API Key"));
        // TODO later optimize in repository
        return new ConsumerApiKeyResponse(
                apiKeyData.getLabel(),
                apiKeyData.getUser().getUsername(),
                apiKeyData.getDomainUrl(),
                apiKeyData.getName(),
                apiKeyData.getApiKeyVersion(),
                apiKeyData.isRevoked()
        );
    }
    public List<ConsumerApiKeyResponse> getAllApiKeysForUser(User user) {
        List<ConsumerApiKeyData> apiKeys = consumerApiKeyRepository.findAllByUser(user);
        return apiKeys.stream()
                .map(apiKeyData -> new ConsumerApiKeyResponse(
                        apiKeyData.getLabel(),
                        apiKeyData.getUser().getUsername(),
                        apiKeyData.getDomainUrl(),
                        apiKeyData.getName(),
                        apiKeyData.getApiKeyVersion(),
                        apiKeyData.isRevoked()
                ))
                .toList();
    }

    public List<ConsumerApiKeyResponse> getAllActiveApiKeysForUser(User user) {
        List<ConsumerApiKeyData> apiKeys = consumerApiKeyRepository.findAllActiveByUser(user);
        return apiKeys.stream()
                .map(apiKeyData -> new ConsumerApiKeyResponse(
                        apiKeyData.getLabel(),
                        apiKeyData.getUser().getUsername(),
                        apiKeyData.getDomainUrl(),
                        apiKeyData.getName(),
                        apiKeyData.getApiKeyVersion(),
                        apiKeyData.isRevoked()
                ))
                .toList();
    }

    public ConsumerApiKeyResponse createConsumerApiKey(String username, String domainUrl, String name) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return createConsumerApiKey(user, domainUrl, name);
    }

    @Transactional
    public ConsumerApiKeyResponse createConsumerApiKey(User user, String domainUrl, String name) {
        var existing = consumerApiKeyRepository.findByUserAndName(user, name);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("API Key with the same name already exists for this user");
        }
        String apiKey = apiKeyGeneratorUtil.generateApiKey();
        String apiKeyHash = hmacHashService.hash(apiKey);
        ConsumerApiKeyData apiKeyData = new ConsumerApiKeyData(
            apiKey,
            apiKeyHash,
            currentVersion,
            user,
            name,
            domainUrl
        );
        apiKeyData = consumerApiKeyRepository.save(apiKeyData);
        //TODO add proper user limits later, 500 for now
        var cachedDetails = CachedUserDetails.fromUser(user, 500, currentVersion);
        apiKeyCache.put(apiKeyHash, cachedDetails);
        return new ConsumerApiKeyResponse(
                apiKey,
                user.getUsername(),
                domainUrl,
                name,
                currentVersion,
                false);
    }

    @Transactional
    public void revokeApiKey(User user, String name) {
        ConsumerApiKeyData apiKeyData = consumerApiKeyRepository.findByUserAndName(user, name)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found for user: " + user.getUsername()));
        apiKeyData.setRevoked(true);
        consumerApiKeyRepository.save(apiKeyData);
        apiKeyCache.evictHash(apiKeyData.getApiKeyHash());
        
    }

    @Transactional
    public void revokeAllApiKeysForUser(User user) {
        List<ConsumerApiKeyData> apiKeys = consumerApiKeyRepository.findAllActiveByUser(user);
        for (var apiKey : apiKeys) {
            apiKey.setRevoked(true);
            consumerApiKeyRepository.save(apiKey);
            apiKeyCache.evictHash(apiKey.getApiKeyHash());
        }
        apiKeyCache.evictUserDetailsByUserId(user.getId());
    }

    @Transactional
    public void deleteAllApiKeysForUser(User user) {
        List<ConsumerApiKeyData> apiKeys = consumerApiKeyRepository.findAllByUser(user);
        for (var apiKey : apiKeys) {
            consumerApiKeyRepository.delete(apiKey);
            apiKeyCache.evictHash(apiKey.getApiKeyHash());
        }
        apiKeyCache.evictUserDetailsByUserId(user.getId());
    }

    private String createLabelForApiKey(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append(key.substring(0, 11));
        sb.append(" **** ");
        sb.append(key.substring(key.length() - 4));
        return sb.toString();
    }
}
