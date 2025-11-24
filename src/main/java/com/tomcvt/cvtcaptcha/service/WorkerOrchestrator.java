package com.tomcvt.cvtcaptcha.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;
import com.tomcvt.cvtcaptcha.workers.CaptchaCleanupQueue;
import com.tomcvt.cvtcaptcha.workers.CaptchaCleanupWorker;

@Service
public class WorkerOrchestrator {
    private final CaptchaCleanupQueue captchaCleanupQueue;
    private final CaptchaDataRepository captchaDataRepository;
    private final String storageDir;

    public WorkerOrchestrator(
        CaptchaCleanupQueue captchaCleanupQueue,
        @Value("${app.captcha-dir}") String storageDir,
            CaptchaDataRepository captchaDataRepository
    ) {
        this.captchaCleanupQueue = captchaCleanupQueue;
        this.storageDir = storageDir;
        this.captchaDataRepository = captchaDataRepository;
        initAndStartWorkers();
    }

    private void initAndStartWorkers() {
        CaptchaCleanupWorker captchaCleanupWorker = new CaptchaCleanupWorker(
            captchaCleanupQueue,
            storageDir,
            captchaDataRepository
        );
        Thread cleanupThread = new Thread(captchaCleanupWorker, "Captcha-Cleanup-Worker");
        cleanupThread.start();
    }




}
