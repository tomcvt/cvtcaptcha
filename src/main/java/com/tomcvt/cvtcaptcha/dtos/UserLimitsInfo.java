package com.tomcvt.cvtcaptcha.dtos;

public record UserLimitsInfo(
    Integer hourlyCaptchaLimit,
    Integer dailyCaptchaLimit,
    String dailyRemainingCaptchaLimit) {
    
}
