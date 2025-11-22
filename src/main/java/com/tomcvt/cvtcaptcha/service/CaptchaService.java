package com.tomcvt.cvtcaptcha.service;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Cleanup;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomcvt.cvtcaptcha.dtos.CIOParameters;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.ExpiredCaptchaException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;

@Service
public class CaptchaService {
    private final SolutionGenerator solutionGenerator;
    private final SolutionVerificationService solutionVerificationService;
    private final CaptchaDataRepository captchaDataRepository;
    private final CaptchaImageGenerator captchaImageGenerator;
    private final CleanupService cleanupService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int captchaExpirationMillis;

    public CaptchaService(SolutionGenerator solutionGenerator,
            SolutionVerificationService solutionVerificationService,
            CaptchaDataRepository captchaDataRepository,
            CaptchaImageGenerator captchaImageGenerator,
            CleanupService cleanupService,
        @Value("${com.tomcvt.captcha.expiration-ms}") int captchaExpirationMillis) {
        this.solutionGenerator = solutionGenerator;
        this.solutionVerificationService = solutionVerificationService;
        this.captchaDataRepository = captchaDataRepository;
        this.captchaImageGenerator = captchaImageGenerator;
        this.cleanupService = cleanupService;
        this.captchaExpirationMillis = captchaExpirationMillis;
    }

    public CaptchaData createCaptcha(UUID requestId, CaptchaType type, String userIp) {
        CaptchaData captchaData = new CaptchaData();
        requestId = captchaData.getRequestId();
        captchaData.setType(type);
        captchaData.setUserIp(userIp);
        captchaData.setCreatedAt(System.currentTimeMillis());
        captchaData.setExpiresAt(System.currentTimeMillis() + captchaExpirationMillis);
        String fileName = null;
        // TODO make switch here
        if (type == CaptchaType.CLICK_IN_ORDER) {
            String solution = solutionGenerator.generateCIOSolution();
            captchaData.setSolution(solution);
            File imageFile = captchaImageGenerator.generateEmojiCaptchaImage(requestId, solution);
            // TODO store in CDN or proper file storage
            String imageData = "/captcha-images/" + imageFile.getName();
            fileName = imageFile.getName();
            captchaData.setData(imageData);
            int clickRadius = 36; // TODO refactor to pixel depending on config and emote size
            String parameters = null;
            try {
                parameters = objectMapper.writeValueAsString(new CIOParameters(clickRadius));
            } catch (Exception e) {
                // TODO logginh and proper handling
                e.printStackTrace();
            }
            captchaData.setParameters(parameters); // parameters to solve resolver
        }
        cleanupService.scheduleCaptchaCleanup(requestId, fileName, captchaData.getExpiresAt());
        return captchaDataRepository.save(captchaData);
    }

    public boolean verifyCaptchaSolution(UUID requestId, CaptchaType type, String solution) {
        CaptchaData captchaData = captchaDataRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Captcha not found"));
        if (captchaData.getExpiresAt() < System.currentTimeMillis()) {
            throw new ExpiredCaptchaException("Captcha has expired");
        }
        // TODO switch later
        if (type == CaptchaType.CLICK_IN_ORDER) {
            return solutionVerificationService.verifyCIOSolution(captchaData.getSolution(), solution);
        }
        return false;
    }
}
