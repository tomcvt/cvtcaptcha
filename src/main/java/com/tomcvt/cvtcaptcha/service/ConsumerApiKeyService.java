package com.tomcvt.cvtcaptcha.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.ConsumerApiKeyRepository;
import com.tomcvt.cvtcaptcha.repository.UserRepository;
import com.tomcvt.cvtcaptcha.utility.ApiKeyGeneratorUtil;

@Service
public class ConsumerApiKeyService {
    private final HmacHashService hmacHashService;
    private final ApiKeyGeneratorUtil apiKeyGeneratorUtil;
    private final ConsumerApiKeyRepository consumerApiKeyRepository;
    private final UserRepository userRepository;
    private final ApiKeyRegistry apiKeyRegistry;

    public ConsumerApiKeyService(
        HmacHashService hmacHashService,
        ConsumerApiKeyRepository consumerApiKeyRepository,
        UserRepository userRepository, 
        ApiKeyRegistry apiKeyRegistry
    ) {
        this.hmacHashService = hmacHashService;
        this.apiKeyGeneratorUtil = new ApiKeyGeneratorUtil();
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.userRepository = userRepository;
        this.apiKeyRegistry = apiKeyRegistry;
    }

    public SecureUserDetails authenticateApiKey(String apiKey) {
        String apiKeyHash = hmacHashService.hash(apiKey);
        ConsumerApiKeyData apiKeyData = apiKeyRegistry.getApiKeyData(apiKeyHash);
        if (apiKeyData == null) {
            //TODO custom exception and logging for ip in security logs
            throw new IllegalArgumentException("Invalid API Key");
        }
        User user = apiKeyData.getUser();
        return new SecureUserDetails(true, user, null);
    }

    public ConsumerApiKeyResponse validateAndGetConsumerApiKeyData(String apiKey) {
        String apiKeyHash = hmacHashService.hash(apiKey);
        return getConsumerApiKeyData(apiKeyHash);
    }

    public ConsumerApiKeyResponse getConsumerApiKeyData(String apiKeyHash) {
        ConsumerApiKeyData apiKeyData = apiKeyRegistry.getApiKeyData(apiKeyHash);
        if (apiKeyData == null) {
            throw new IllegalArgumentException("API Key not found");
        }
        //TODO later optimize in repository
        return new ConsumerApiKeyResponse(
            null,
            apiKeyData.getUser().getUsername(),
            apiKeyData.getDomainUrl(),
            apiKeyData.getName()
        );
    }

    public ConsumerApiKeyResponse createConsumerApiKey(String username, String domainUrl, String name) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return createConsumerApiKey(user, domainUrl, name);
    }

    @Transactional
    public ConsumerApiKeyResponse createConsumerApiKey(User user, String domainUrl, String name) {
        String apiKey = apiKeyGeneratorUtil.generateApiKey();
        String apiKeyHash = hmacHashService.hash(apiKey);
        ConsumerApiKeyData apiKeyData = new ConsumerApiKeyData();
        apiKeyData.setUser(user);
        apiKeyData.setDomainUrl(domainUrl);
        apiKeyData.setName(name);
        apiKeyData.setApiKeyHash(apiKeyHash);
        apiKeyData = consumerApiKeyRepository.save(apiKeyData);
        apiKeyRegistry.registerApiKeyHash(apiKeyHash, apiKeyData);
        return new ConsumerApiKeyResponse(
            apiKey,
            user.getUsername(),
            domainUrl,
            name
        );
    }
}
