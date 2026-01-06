package com.tomcvt.cvtcaptcha.controller.api;

import java.util.List;

import org.attoparser.dom.Text;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.auth.AuthService;
import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.dtos.ApiKeyRequest;
import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.dtos.PassPayload;
import com.tomcvt.cvtcaptcha.dtos.TextResponse;
import com.tomcvt.cvtcaptcha.dtos.UserLimitsInfo;
import com.tomcvt.cvtcaptcha.exceptions.WrongAuthenticationMethodException;
import com.tomcvt.cvtcaptcha.service.ConsumerApiKeyService;

@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER', 'TOMCVT')")
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserApiController.class);
    private final ConsumerApiKeyService consumerApiKeyService;
    private final AuthService authService;
    //private static final ErrorResponse STANDARD_AUTH_ERROR = new ErrorResponse("WRONG_AUTH_METHOD", "Authenticate using standard method (browser)");
    private static final ErrorResponse STANDARD_FORBIDDEN_ERROR = new ErrorResponse("FORBIDDEN", "You do not have permission to perform this action");


    public UserApiController(ConsumerApiKeyService consumerApiKeyService, AuthService authService) {
        this.consumerApiKeyService = consumerApiKeyService;
        this.authService = authService;
    }

    @PostMapping("/api-keys/create")
    public ResponseEntity<?> createApiKey(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ApiKeyRequest request) {
        SecureUserDetails cas = getSecureUserDetails(userDetails);
        if (cas == null) {
            return ResponseEntity.status(403).body(
                STANDARD_FORBIDDEN_ERROR
            );
        }
        ConsumerApiKeyResponse response = consumerApiKeyService.createConsumerApiKey(
            cas.getUser(),
            request.domainUrl(),
            request.name()
        );
        log.info("API key created for user: {}", cas.getUser().getUsername());
        return ResponseEntity.ok(response);
    }
    //TODO refactor to mapping in controller and returining objects in service
    @GetMapping("/api-keys/list")
    public ResponseEntity<?> listApiKeys(@AuthenticationPrincipal UserDetails userDetails) {
        SecureUserDetails cas = getSecureUserDetails(userDetails);
        List<ConsumerApiKeyResponse> apiKeys = consumerApiKeyService.getAllApiKeysForUser(cas.getUser());
        return ResponseEntity.ok(apiKeys);
    }
    @PostMapping("/api-keys/revoke")
    public ResponseEntity<?> revokeApiKey(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ApiKeyRequest request) {
        SecureUserDetails cas = getSecureUserDetails(userDetails);
        consumerApiKeyService.revokeApiKey(cas.getUser(), request.name());
        log.info("API key '{}' revoked for user: {}", request.name(), cas.getUser().getUsername());
        return ResponseEntity.ok(new TextResponse("API key revoked successfully"));
    }

    @GetMapping("/limits")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        SecureUserDetails cas = getSecureUserDetails(userDetails);
        UserLimitsInfo userLimits = consumerApiKeyService.getUserLimitsInfoByUser(cas.getUser());
        return ResponseEntity.ok(userLimits);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PassPayload payload) {
        SecureUserDetails cas = getSecureUserDetails(userDetails);
        authService.changePassword(cas.getUser(), payload);
        return ResponseEntity.ok(new TextResponse("Password changed successfully"));
    }

    private SecureUserDetails getSecureUserDetails(UserDetails userDetails) {
        SecureUserDetails cas = null;
        try {
            cas = (SecureUserDetails) userDetails;
        } catch (ClassCastException e) {
            throw new WrongAuthenticationMethodException("Wrong authentication method used");
        }
        if (cas == null) {
            throw new AuthorizationDeniedException("Unauthorized");
        }
        return cas;
    }
}