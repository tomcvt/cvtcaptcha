package com.tomcvt.cvtcaptcha.service;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomcvt.cvtcaptcha.dtos.CIOParameters;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.exceptions.ExpiredCaptchaException;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.records.CaptchaCleanupTask;
import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;
import com.tomcvt.cvtcaptcha.workers.CaptchaCleanupQueue;

@Service
public class CaptchaService {
    private final SolutionGenerator solutionGenerator;
    private final SolutionVerificationService solutionVerificationService;
    private final CaptchaDataRepository captchaDataRepository;
    private final CaptchaImageGenerator captchaImageGenerator;
    private final CaptchaCleanupQueue captchaCleanupQueue;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int captchaExpirationMillis;
    private final String urlOrigin;
    private final int maxQueueSize = 10000;

    //image size about 10.03kB meaning we can 10k captcha safely 
    // track and throw exceptions if limits are exceeded

    public CaptchaService(SolutionGenerator solutionGenerator,
            SolutionVerificationService solutionVerificationService,
            CaptchaDataRepository captchaDataRepository,
            CaptchaImageGenerator captchaImageGenerator,
            CaptchaCleanupQueue captchaCleanupQueue,
        @Value("${com.tomcvt.captcha.expiration-ms}") int captchaExpirationMillis,
        @Value("${com.tomcvt.url-origin}") String urlOrigin){
        this.solutionGenerator = solutionGenerator;
        this.solutionVerificationService = solutionVerificationService;
        this.captchaDataRepository = captchaDataRepository;
        this.captchaImageGenerator = captchaImageGenerator;
        this.captchaCleanupQueue = captchaCleanupQueue;
        this.captchaExpirationMillis = captchaExpirationMillis;
        this.urlOrigin = urlOrigin;
    }

    public CaptchaData createCaptcha(UUID requestId, CaptchaType type, String userIp) {
        if (captchaCleanupQueue.getQueueSize() >= maxQueueSize) {
            throw new IllegalStateException("Captcha generation queue is full. Please try again later.");
        }
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
            String imageData = urlOrigin + "/captcha-images/" + imageFile.getName();
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
        CaptchaCleanupTask task = new CaptchaCleanupTask(
            requestId,
            fileName,
            captchaData.getExpiresAt()
        );
        captchaCleanupQueue.offer(task);
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
    
    @Transactional

    public void deleteCaptcha(UUID requestId) {
        captchaDataRepository.deleteByRequestId(requestId);
    }
}
