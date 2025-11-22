package com.tomcvt.cvtcaptcha.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CleanupService {
    private final String storageDir;
    private final ScheduledExecutorService scheduler;

    public CleanupService(@Value("${app.captcha-dir}") String storageDir) {
        this.storageDir = storageDir;
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public void scheduleCaptchaCleanup(UUID requestId, String fileName, long expiresAt) {
        long currentTimeMillis = System.currentTimeMillis();
        long delayMillis = expiresAt - currentTimeMillis;
        scheduler.schedule(() -> {
            // Perform cleanup logic here
            System.out.println("Cleaning up captcha with requestId: " + requestId + " and fileName: " + fileName);
            // e.g., delete captcha data from database and remove image file
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void deleteCaptchaImage(String fileName) {
        // Implement file deletion logic here
        System.out.println("Deleting captcha image file: " + fileName);

    }
}