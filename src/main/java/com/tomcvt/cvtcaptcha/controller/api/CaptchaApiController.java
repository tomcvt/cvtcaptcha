package com.tomcvt.cvtcaptcha.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.dtos.CaptchaRequest;
import com.tomcvt.cvtcaptcha.dtos.CaptchaResponse;
import com.tomcvt.cvtcaptcha.dtos.CaptchaTokenResponse;
import com.tomcvt.cvtcaptcha.dtos.ErrorResponse;
import com.tomcvt.cvtcaptcha.dtos.SolutionResponse;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.WrongTypeException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.network.CaptchaRateLimiter;
import com.tomcvt.cvtcaptcha.service.CaptchaService;
import com.tomcvt.cvtcaptcha.service.CaptchaTokenService;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaApiController {
    private final CaptchaService captchaService;
    private final CaptchaRateLimiter captchaRateLimiter;
    private final CaptchaTokenService captchaTokenService;

    public CaptchaApiController(CaptchaService captchaService, CaptchaRateLimiter captchaRateLimiter, 
                                CaptchaTokenService captchaTokenService
    ) {
        this.captchaService = captchaService;
        this.captchaRateLimiter = captchaRateLimiter;
        this.captchaTokenService = captchaTokenService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCaptcha(@AuthenticationPrincipal SecureUserDetails userDetails, 
                                                      @RequestBody CaptchaRequest captchaRequest) {
        CaptchaData captcha = null;
        CaptchaType type = parseCaptchaType(captchaRequest.type());
        if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ANON"))) {
            captchaRateLimiter.checkAndIncrementAnonymousLimit(userDetails.getIp());
            captcha = captchaService.createCaptcha(captchaRequest.requestId(), type, userDetails.getIp());
        }
        if (captcha == null) {
            return ResponseEntity.status(500).body("Captcha creation failed");
        }
        //TODO for now in url return the parsed solution
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
            //TODO generate real token
            String token = captchaTokenService.generateCaptchaToken(solutionResponse.requestId().toString());
            return ResponseEntity.ok(new CaptchaTokenResponse(token));
        } else {
            return ResponseEntity.status(400).body(new ErrorResponse("WRONG_SOLUTION", "The provided solution is incorrect."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestParam String token) {
        String requestId = captchaTokenService.validateCaptchaToken(token);
        //TODO decide on what o return
        return ResponseEntity.ok().body("Captcha verified for requestId: " + requestId);
    }

    private CaptchaType parseCaptchaType(String typeStr) {
        try {
            return CaptchaType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new WrongTypeException("Invalid captcha type: " + typeStr);
        }
    }
}
