package com.tomcvt.cvtcaptcha.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tomcvt.cvtcaptcha.auth.AuthService;
import com.tomcvt.cvtcaptcha.dtos.ApiKeyRequest;
import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.dtos.CreateUserRequest;
import com.tomcvt.cvtcaptcha.enums.UserTypes;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.service.ConsumerApiKeyService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN','SUPERUSER')")
public class AdminApiController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminApiController.class);
    private final ConsumerApiKeyService consumerApiKeyService;
    private final AuthService authService;

    public AdminApiController(ConsumerApiKeyService consumerApiKeyService,
        AuthService authService
    ) {
        this.consumerApiKeyService = consumerApiKeyService;
        this.authService = authService;
    }

    @PostMapping("/create-consumer-api-key")
    public ResponseEntity<?> createConsumerApiKey(@RequestBody ApiKeyRequest request) {
        validateApiKeyRequest(request);
        ConsumerApiKeyResponse response = consumerApiKeyService.createConsumerApiKey(
            request.username(),
            request.domainUrl(),
            request.name()
        );
        log.info("Created consumer API key for user: {}, domainUrl: {}", request.username(), request.domainUrl());
        return ResponseEntity.ok(response);
    }
    //TODO change to json
    @GetMapping("/verify-api-key")
    public ResponseEntity<?> verifyApiKey(@RequestParam String apiKey) {
        ConsumerApiKeyResponse response = consumerApiKeyService.validateAndGetConsumerApiKeyData(apiKey);
        return ResponseEntity.ok(response);
    }

    private void validateApiKeyRequest(ApiKeyRequest request) {
        if (request.username() == null || request.username().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.domainUrl() == null || request.domainUrl().isEmpty()) {
            throw new IllegalArgumentException("Domain URL is required");
        }
        if (request.name() == null || request.name().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        String role = validateUserType(request.role());
        User user = authService.registerActivatedUser(
            request.username(),
            request.password(),
            request.email(),
            role
        );
        return ResponseEntity.ok("User created successfully");
    }


    private String validateUserType(String role) {
        try {
            var ut = UserTypes.valueOf(role.toUpperCase());
            return ut.name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }
}
