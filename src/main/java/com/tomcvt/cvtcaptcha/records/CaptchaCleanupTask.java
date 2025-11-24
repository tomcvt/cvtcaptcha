package com.tomcvt.cvtcaptcha.records;

import java.util.UUID;

public record CaptchaCleanupTask(
    UUID requestId,
    String fileName,
    long scheduledTimeMillis
) {
    
}
