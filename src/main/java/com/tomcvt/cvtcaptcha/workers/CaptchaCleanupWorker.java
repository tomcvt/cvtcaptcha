package com.tomcvt.cvtcaptcha.workers;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import com.tomcvt.cvtcaptcha.records.CaptchaCleanupTask;
import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;

public class CaptchaCleanupWorker implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CaptchaCleanupWorker.class);
    private final CaptchaCleanupQueue taskQueue;
    private final String storageDir;
    private final CaptchaDataRepository captchaDataRepository;
    private volatile boolean running = true;
    private volatile boolean paused = false;

    public CaptchaCleanupWorker(CaptchaCleanupQueue taskQueue,
            String storageDir,
            CaptchaDataRepository captchaDataRepository) {
        this.taskQueue = taskQueue;
        this.storageDir = storageDir;
        this.captchaDataRepository = captchaDataRepository;
    }

    public void shutdown() {
        running = false;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (this) {
            paused = false;
            this.notifyAll();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                synchronized (this) {
                    while (paused) {
                        this.wait();
                    }
                }

                CaptchaCleanupTask task = taskQueue.take();
                long currentTimeMillis = System.currentTimeMillis();
                if (task.scheduledTimeMillis() > currentTimeMillis) {
                    // just w8 and process
                    Thread.sleep(task.scheduledTimeMillis() - currentTimeMillis);
                }
                processTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processTask(CaptchaCleanupTask task) {
        deleteCaptchaImage(task.requestId(), task.fileName());
        deleteCaptchaData(task.requestId());
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

    private void deleteCaptchaData(UUID requestId) {
        try {
            captchaDataRepository.deleteByRequestId(requestId);
        } catch (Exception e) {
            log.error("Failed to delete captcha data for requestId: {}", requestId, e);
        }
        log.info("Deleted captcha data for requestId: {}", requestId);
    }
}
