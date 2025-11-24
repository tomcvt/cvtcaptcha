package com.tomcvt.cvtcaptcha.workers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.records.CaptchaCleanupTask;

@Component
public class CaptchaCleanupQueue {
    BlockingQueue<CaptchaCleanupTask> taskQueue;

    public CaptchaCleanupQueue() {
        this.taskQueue = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<CaptchaCleanupTask> getTaskQueue() {
        return taskQueue;
    }

    public void offer(CaptchaCleanupTask task) {
        taskQueue.offer(task);
    }

    public CaptchaCleanupTask take() throws InterruptedException {
        return taskQueue.take();
    }

}
