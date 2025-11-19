package com.tomcvt.cvtcaptcha.dtos;

import java.util.UUID;

public record VerificationResponse(UUID requestId, boolean solved) {
    
}
