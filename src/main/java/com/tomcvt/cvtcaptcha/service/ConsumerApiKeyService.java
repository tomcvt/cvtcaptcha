package com.tomcvt.cvtcaptcha.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ConsumerApiKeyService(
        HmacHashService hmacHashService,
        ConsumerApiKeyRepository consumerApiKeyRepository,
        UserRepository userRepository
    ) {
        this.hmacHashService = hmacHashService;
        this.apiKeyGeneratorUtil = new ApiKeyGeneratorUtil();
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.userRepository = userRepository;
    }

    public String createConsumerApiKey(String username, String domainUrl, String name) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return createConsumerApiKey(user, domainUrl, name);
    }

    @Transactional
    public String createConsumerApiKey(User user, String domainUrl, String name) {
        String apiKey = apiKeyGeneratorUtil.generateApiKey();
        String apiKeyHash = hmacHashService.hash(apiKey);
        ConsumerApiKeyData apiKeyData = new ConsumerApiKeyData();
        apiKeyData.setUser(user);
        apiKeyData.setDomainUrl(domainUrl);
        apiKeyData.setName(name);
        apiKeyData.setApiKeyHash(apiKeyHash);
        

    }
}
