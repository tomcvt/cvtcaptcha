package com.tomcvt.cvtcaptcha.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.proc.SecurityContext;
import com.tomcvt.cvtcaptcha.auth.CachedUserDetails;
import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.auth.WebIpAuthenticationDetails;
import com.tomcvt.cvtcaptcha.dtos.CaptchaRequest;
import com.tomcvt.cvtcaptcha.dtos.CaptchaResponse;
import com.tomcvt.cvtcaptcha.dtos.CaptchaTokenResponse;
import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.dtos.SolutionResponse;
import com.tomcvt.cvtcaptcha.dtos.VerificationResponse;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.CaptchaLimitExceededException;
import com.tomcvt.cvtcaptcha.exceptions.WrongTypeException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.network.CaptchaRateLimiter;
import com.tomcvt.cvtcaptcha.service.CaptchaService;
import com.tomcvt.cvtcaptcha.service.CaptchaTokenService;

import io.micrometer.core.ipc.http.HttpSender.Response;
import jakarta.servlet.http.HttpServletRequest;

@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER', 'TOMCVT')")
@RestController
@RequestMapping("/api/captcha")
public class CaptchaApiController {
    private final CaptchaService captchaService;
    private final CaptchaRateLimiter captchaRateLimiter;
    private final CaptchaTokenService captchaTokenService;
    private final List<String> limitedConsumers = List.of("ROLE_USER", "ROLE_ADMIN");
    private final List<String> unlimitedConsumers = List.of("ROLE_SUPERUSER", "ROLE_TOMCVT");

    public CaptchaApiController(CaptchaService captchaService, CaptchaRateLimiter captchaRateLimiter,
            CaptchaTokenService captchaTokenService) {
        this.captchaService = captchaService;
        this.captchaRateLimiter = captchaRateLimiter;
        this.captchaTokenService = captchaTokenService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCaptcha(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CaptchaRequest captchaRequest) {
        try {
            CachedUserDetails cud = (CachedUserDetails) userDetails;
            cud.useRequest();
        } catch (ClassCastException e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("AUTHENTICATION_ERROR", "Authenticate using an API key to access this endpoint"));
        } catch (CaptchaLimitExceededException e) {
            return ResponseEntity.status(429)
                    .body(new ErrorResponse("LIMIT_EXCEEDED", "No remaining requests available for this API key"));
        }
        CaptchaData captcha = null;
        CaptchaType type = parseCaptchaType(captchaRequest.type());
        WebIpAuthenticationDetails details = (WebIpAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        captcha = captchaService.createCaptcha(captchaRequest.requestId(), type,
                details.getIpAddress());
        CaptchaResponse response = new CaptchaResponse(
                captcha.getRequestId(),
                captcha.getData());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solve")
    public ResponseEntity<?> solveCaptcha(@RequestBody SolutionResponse solutionResponse) {
        CaptchaType type = parseCaptchaType(solutionResponse.type());
        if (captchaService.verifyCaptchaSolution(solutionResponse.requestId(), type, solutionResponse.solution())) {
            String token = captchaTokenService.generateCaptchaToken(solutionResponse.requestId().toString());
            return ResponseEntity.ok(new CaptchaTokenResponse(token));
        } else {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("WRONG_SOLUTION", "The provided solution is incorrect."));
        }
    }

    // TODO refactor responses
    @GetMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestParam(required = false) String token, HttpServletRequest request) {
        String tokenH = request.getHeader("X-Captcha-Token");
        if (tokenH != null && !tokenH.isEmpty()) {
            token = tokenH;
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("MISSING_TOKEN", "Captcha token is required for verification."));
        }
        UUID requestId;
        try {
            String requestIdStr = captchaTokenService.validateCaptchaToken(token);
            requestId = UUID.fromString(requestIdStr);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("INVALID_TOKEN", e.getMessage()));
        }
        var response = new VerificationResponse(requestId, true);
        return ResponseEntity.ok().body(response);
    }

    private CaptchaType parseCaptchaType(String typeStr) {
        try {
            return CaptchaType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new WrongTypeException("Invalid captcha type: " + typeStr);
        }
    }
}
