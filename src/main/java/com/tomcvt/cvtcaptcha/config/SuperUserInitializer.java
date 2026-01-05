package com.tomcvt.cvtcaptcha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.auth.CachedUserDetails;
import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;
import com.tomcvt.cvtcaptcha.repository.ConsumerApiKeyRepository;
import com.tomcvt.cvtcaptcha.repository.UserLimitsRepository;
import com.tomcvt.cvtcaptcha.repository.UserRepository;
import com.tomcvt.cvtcaptcha.service.ApiKeyCache;
import com.tomcvt.cvtcaptcha.service.ConsumerApiKeyService;
import com.tomcvt.cvtcaptcha.service.HmacHashService;

@Service
public class SuperUserInitializer {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SuperUserInitializer.class);
    private final UserRepository userRepository;
    private final UserLimitsRepository userLimitsRepository;
    private final PasswordEncoder passwordEncoder;
    private final HmacHashService hmacHashService;
    private final ConsumerApiKeyRepository consumerApiKeyRepository;
    private final ConsumerApiKeyService consumerApiKeyService;
    private final ApiKeyCache apiKeyCache;
    private final String superUsername;
    private final String superPassword;
    private final String superuserApiKey;
    private final String superuserApiKeyVersion;

    public SuperUserInitializer(UserRepository userRepository, 
        PasswordEncoder passwordEncoder,
        HmacHashService hmacHashService,
        ConsumerApiKeyRepository consumerApiKeyRepository,
        ConsumerApiKeyService consumerApiKeyService,
        UserLimitsRepository userLimitsRepository,
        ApiKeyCache apiKeyCache,
        @Value("${com.tomcvt.superuser.api-key}") String superuserApiKey,
        @Value("${com.tomcvt.superuser.username}") String superUsername,
        @Value("${com.tomcvt.superuser.password}") String superPassword,
        @Value("${com.tomcvt.superuser.api-key-version}") String superuserApiKeyVersion) {
        this.userRepository = userRepository;
        this.userLimitsRepository = userLimitsRepository;
        this.superUsername = superUsername;
        this.superPassword = superPassword;
        this.passwordEncoder = passwordEncoder;
        this.superuserApiKey = superuserApiKey;
        this.superuserApiKeyVersion = superuserApiKeyVersion;
        this.hmacHashService = hmacHashService;
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.consumerApiKeyService = consumerApiKeyService;
        this.apiKeyCache = apiKeyCache;
    }

    
    /**
     * Initializes the superuser account and associated API key.
     * <p>
     * If a superuser already exists, updates their password and API key.
     * Otherwise, creates a new superuser with the specified credentials.
     * Removes any existing API keys for the superuser and registers a new one,
     * ensuring that the superuser always has a valid API key properly versioned.
     * </p>
     * 
     * <p>
     * This method is transactional to ensure atomicity of user and API key updates.
     * </p>
     */
    @Transactional
    public void init() {
        var superUserOpt = userRepository.findByUsername(superUsername);
        if (superUserOpt.isPresent()) {
            log.info("Superuser already exists, updating API key and password.");
            User superUser = superUserOpt.get();
            String encodedPassword = passwordEncoder.encode(superPassword);
            superUser.setPassword(encodedPassword);
            userRepository.save(superUser);
        }
        User superUser = null;
        if (superUserOpt.isEmpty()) {
            superUser = new User();
            superUser.setUsername(superUsername);
            String encodedPassword = passwordEncoder.encode(superPassword);
            superUser.setPassword(encodedPassword);
            superUser.setEmail("szalolony@gmail.com");
            superUser.setRole("SUPERUSER");
            superUser.setEnabled(true);
            superUser = userRepository.save(superUser);
            log.info("Superuser created successfully.");
        }
        consumerApiKeyService.deleteAllApiKeysForUser(superUserOpt.orElse(superUser));
        var superUserLimits = userLimitsRepository.findByUserId(superUser.getId());
        if (superUserLimits.isEmpty()) {
            var limits = new UserLimits();
            limits.setUser(superUser);
            limits.setHourlyCaptchaLimit(10000);
            limits.setDailyCaptchaLimit(50000);
            userLimitsRepository.save(limits);
            log.info("Superuser limits initialized.");
        }
        ConsumerApiKeyData consumerApiKeyData = new ConsumerApiKeyData(
            superuserApiKey,
            null,
            superuserApiKeyVersion,
            null,
            "Superuser API Key",
            "http://localhost"
        );
        consumerApiKeyData.setUser(superUser);
        String apiKeyHash = hmacHashService.hash(superuserApiKey);
        consumerApiKeyData.setApiKeyHash(apiKeyHash);
        consumerApiKeyData = consumerApiKeyRepository.save(consumerApiKeyData);
        var superuserCachedDetails = CachedUserDetails.fromUser(superUser, null, superuserApiKeyVersion);
        apiKeyCache.put(apiKeyHash, superuserCachedDetails);
        log.info("Superuser API key initialized/updated successfully.");
    }
}
