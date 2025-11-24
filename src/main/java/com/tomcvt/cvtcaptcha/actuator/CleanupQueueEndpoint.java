package com.tomcvt.cvtcaptcha.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.workers.CaptchaCleanupQueue;

@Component
@Endpoint(id = "cleanupQueue")
public class CleanupQueueEndpoint {
    private final CaptchaCleanupQueue cleanupQueue;

    public CleanupQueueEndpoint(CaptchaCleanupQueue cleanupQueue) {
        this.cleanupQueue = cleanupQueue;
    }

    @ReadOperation
    public Object getQueue() {
        return cleanupQueue.getQueueSnapshot();
    }
}
