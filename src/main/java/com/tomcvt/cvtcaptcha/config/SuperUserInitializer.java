package com.tomcvt.cvtcaptcha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.ConsumerApiKeyRepository;
import com.tomcvt.cvtcaptcha.repository.UserRepository;
import com.tomcvt.cvtcaptcha.service.ApiKeyRegistry;
import com.tomcvt.cvtcaptcha.service.HmacHashService;

@Service
public class SuperUserInitializer {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SuperUserInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HmacHashService hmacHashService;
    private final ConsumerApiKeyRepository consumerApiKeyRepository;
    private final ApiKeyRegistry apiKeyRegistry;
    private final String superUsername;
    private final String superPassword;
    private final String superuserApiKey;

    public SuperUserInitializer(UserRepository userRepository, 
        PasswordEncoder passwordEncoder,
        HmacHashService hmacHashService,
        ConsumerApiKeyRepository consumerApiKeyRepository,
        ApiKeyRegistry apiKeyRegistry,
        @Value("${com.tomcvt.superuser.api-key}") String superuserApiKey,
        @Value("${com.tomcvt.superuser.username}") String superUsername,
        @Value("${com.tomcvt.superuser.password}") String superPassword) {
        this.userRepository = userRepository;
        this.superUsername = superUsername;
        this.superPassword = superPassword;
        this.passwordEncoder = passwordEncoder;
        this.superuserApiKey = superuserApiKey;
        this.hmacHashService = hmacHashService;
        this.consumerApiKeyRepository = consumerApiKeyRepository;
        this.apiKeyRegistry = apiKeyRegistry;
    }

    public void init() {
        var superUserOpt = userRepository.findByUsername(superUsername);
        if (superUserOpt.isPresent()) {
            log.info("Superuser already exists, skipping creation.");
            return;
        }
        User superUser = null;
        if (superUserOpt.isEmpty()) {
            superUser = new User();
            superUser.setUsername(superUsername);
            superUser.setPassword(passwordEncoder.encode(superPassword));
            superUser.setEmail("");
            superUser.setRole("SUPERUSER");
            superUser.setEnabled(true);
            superUser = userRepository.save(superUser);
        }
        ConsumerApiKeyData consumerApiKeyData = new ConsumerApiKeyData();
        consumerApiKeyData.setUser(superUser);
        String apiKeyHash = hmacHashService.hash(superuserApiKey);
        consumerApiKeyData.setApiKeyHash(apiKeyHash);
        consumerApiKeyData = consumerApiKeyRepository.save(consumerApiKeyData);
        apiKeyRegistry.registerApiKeyHash(apiKeyHash, consumerApiKeyData);
    }
}
