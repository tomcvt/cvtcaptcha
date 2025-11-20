package com.tomcvt.cvtcaptcha.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tomcvt.cvtcaptcha.auth.SecureUserDetails;
import com.tomcvt.cvtcaptcha.dtos.CaptchaRequest;
import com.tomcvt.cvtcaptcha.dtos.CaptchaResponse;
import com.tomcvt.cvtcaptcha.dtos.SolutionResponse;
import com.tomcvt.cvtcaptcha.dtos.VerificationResponse;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.WrongTypeException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.network.CaptchaRateLimiter;
import com.tomcvt.cvtcaptcha.service.CaptchaService;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaApiController {
    private final CaptchaService captchaService;
    private final CaptchaRateLimiter captchaRateLimiter;

    public CaptchaApiController(CaptchaService captchaService, CaptchaRateLimiter captchaRateLimiter) {
        this.captchaService = captchaService;
        this.captchaRateLimiter = captchaRateLimiter;
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

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCaptcha(@RequestBody SolutionResponse solutionResponse) {
        CaptchaType type = parseCaptchaType(solutionResponse.type());
        //TODO add solution tries limit
        System.out.println("Verifying captcha solution for requestId: " + solutionResponse.requestId());
        System.out.println("Solution: " + solutionResponse.solution());
        if(captchaService.verifyCaptchaSolution(solutionResponse.requestId(), type, solutionResponse.solution())) {
            return ResponseEntity.ok(new VerificationResponse(solutionResponse.requestId(), true));
        }
        return ResponseEntity.ok("Not implemented yet");
    }




    private CaptchaType parseCaptchaType(String typeStr) {
        try {
            return CaptchaType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new WrongTypeException("Invalid captcha type: " + typeStr);
        }
    }
}
