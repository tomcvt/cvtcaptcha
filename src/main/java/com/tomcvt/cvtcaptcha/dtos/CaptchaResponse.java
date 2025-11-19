package com.tomcvt.cvtcaptcha.dtos;

import java.util.UUID;

public record CaptchaResponse(UUID requestId, String imageUrl) {
    
}
