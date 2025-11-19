package com.tomcvt.cvtcaptcha.dtos;

import java.util.List;
import java.util.UUID;

public record SolutionResponse(
    UUID requestId,
    String type,
    List<String> solution
) {
    
}
