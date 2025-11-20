package com.tomcvt.cvtcaptcha.dtos;

import java.util.UUID;

public record SolutionResponse(
    UUID requestId,
    String type,
    String solution
) {
    
}
