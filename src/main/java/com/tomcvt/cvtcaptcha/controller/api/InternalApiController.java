package com.tomcvt.cvtcaptcha.controller.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.dtos.CaptchaRequest;
import com.tomcvt.cvtcaptcha.dtos.CaptchaResponse;
import com.tomcvt.cvtcaptcha.dtos.CaptchaTokenResponse;
import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.dtos.SolutionResponse;
import com.tomcvt.cvtcaptcha.dtos.VerificationResponse;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.WrongTypeException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.network.CaptchaRateLimiter;
import com.tomcvt.cvtcaptcha.network.UserRateLimiter;
import com.tomcvt.cvtcaptcha.service.CaptchaService;
import com.tomcvt.cvtcaptcha.service.CaptchaTokenService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/internal")
public class InternalApiController {
    private final CaptchaService captchaService;
    private final CaptchaRateLimiter captchaRateLimiter;
    private final UserRateLimiter userRateLimiter;
    private final CaptchaTokenService captchaTokenService;
    private final List<String> limitedConsumers = List.of("ROLE_USER", "ROLE_ADMIN");
    private final List<String> unlimitedConsumers = List.of("ROLE_SUPERUSER", "ROLE_TOMCVT");

    public InternalApiController(CaptchaService captchaService, CaptchaRateLimiter captchaRateLimiter, 
                                CaptchaTokenService captchaTokenService, UserRateLimiter userRateLimiter
    ) {
        this.captchaService = captchaService;
        this.captchaRateLimiter = captchaRateLimiter;
        this.captchaTokenService = captchaTokenService;
        this.userRateLimiter = userRateLimiter;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCaptcha(@AuthenticationPrincipal SecureUserDetails userDetails, 
                                                      @RequestBody CaptchaRequest captchaRequest) {
        CaptchaData captcha = null;
        CaptchaType type = parseCaptchaType(captchaRequest.type());
        if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ANON"))) {
            //TODO change to anon rate limiter
            captchaRateLimiter.checkAndIncrementAnonymousLimit(userDetails.getIp());
            captcha = captchaService.createCaptcha(captchaRequest.requestId(), type, userDetails.getIp());
        }
        if (userDetails.getAuthorities().stream().anyMatch(auth -> limitedConsumers.contains(auth.getAuthority()))) {
            userRateLimiter.checkAndIncrementUserCaptchaLimit(userDetails.getUser());
            captcha = captchaService.createCaptcha(captchaRequest.requestId(), type, userDetails.getIp());
        }
        if (userDetails.getAuthorities().stream().anyMatch(auth -> unlimitedConsumers.contains(auth.getAuthority()))) {
            captcha = captchaService.createCaptcha(captchaRequest.requestId(), type, userDetails.getIp());
        }
        if (captcha == null) {
            return ResponseEntity.status(500).body("Captcha creation failed");
        }
        CaptchaResponse response = new CaptchaResponse(
            captcha.getRequestId(), 
            captcha.getData()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solve")
    public ResponseEntity<?> solveCaptcha(@RequestBody SolutionResponse solutionResponse) {
        CaptchaType type = parseCaptchaType(solutionResponse.type());
        if(captchaService.verifyCaptchaSolution(solutionResponse.requestId(), type, solutionResponse.solution())) {
            String token = captchaTokenService.generateCaptchaToken(solutionResponse.requestId().toString());
            return ResponseEntity.ok(new CaptchaTokenResponse(token));
        } else {
            return ResponseEntity.status(400).body(new ErrorResponse("WRONG_SOLUTION", "The provided solution is incorrect."));
        }
    }
    //TODO refactor responses
    @GetMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestParam(required = false) String token, HttpServletRequest request) {
        String tokenH = request.getHeader("X-Captcha-Token");
        if (tokenH != null && !tokenH.isEmpty()) {
            token = tokenH;
        }
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(400).body(new ErrorResponse("MISSING_TOKEN", "Captcha token is required for verification."));
        }
        UUID requestId = UUID.fromString(captchaTokenService.validateCaptchaToken(token));
        var response = new VerificationResponse(requestId, true);
        // error throwed returns 401 and error json
        //TODO decide on what o return
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