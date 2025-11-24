package com.tomcvt.cvtcaptcha.workers;

import java.util.HashMap;
import java.util.Map;
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

    public Map<Integer, String> getQueueSnapshot() {
        Map<Integer, String> pendingTasks = new HashMap<>();
        int counter = 1;
        for (CaptchaCleanupTask task : taskQueue) {
            pendingTasks.put(counter, task.toString());
            counter++;
        }
        return pendingTasks;
    }

}
