package com.tomcvt.cvtcaptcha.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tomcvt.cvtcaptcha.dtos.ApiKeyRequest;
import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.service.ConsumerApiKeyService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminApiController.class);
    private final ConsumerApiKeyService consumerApiKeyService;

    public AdminApiController(ConsumerApiKeyService consumerApiKeyService) {
        this.consumerApiKeyService = consumerApiKeyService;
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
}
