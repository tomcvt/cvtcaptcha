package com.tomcvt.cvtcaptcha.dtos;

import java.util.UUID;

public record CaptchaRequest(UUID requestId, String type) {
    
}
