package com.tomcvt.cvtcaptcha.service;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;

@Service
public class CleanupService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CleanupService.class);
    private final String storageDir;
    private final ScheduledExecutorService scheduler;
    private final CaptchaDataRepository captchaDataRepository;

    public CleanupService(@Value("${app.captcha-dir}") String storageDir, 
            CaptchaDataRepository captchaDataRepository) {
        this.storageDir = storageDir;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.captchaDataRepository = captchaDataRepository;
    }

    public void scheduleCaptchaCleanup(UUID requestId, String fileName, long expiresAt) {
        long currentTimeMillis = System.currentTimeMillis();
        long delayMillis = expiresAt - currentTimeMillis;
        scheduler.schedule(() -> {
            deleteCaptchaImage(requestId, fileName);
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void deleteCaptchaImage(UUID requestId, String fileName) {
        // Implement file deletion logic here
        File file = new File(storageDir, fileName);
        try {
            Files.delete(file.toPath());
            var captchaData = captchaDataRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new RuntimeException("Captcha data not found for requestId: " + requestId));
            captchaData.setData("DELETED");
        } catch (Exception e) {
            log.error("Failed to delete captcha image file: {}", fileName, e);
        }
        log.info("Deleted captcha image file: {}", fileName);
    }
}