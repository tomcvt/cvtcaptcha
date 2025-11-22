package com.tomcvt.cvtcaptcha.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;
import com.tomcvt.cvtcaptcha.repository.UserLimitsRepository;

@Service
public class UserLimitsService {
    private final int defaultHourlyCaptchaLimit;
    private final int defaultDailyCaptchaLimit;
    private final int defaultHourlyRequestLimit;
    private final int defaultDailyRequestLimit;
    private final UserLimitsRepository userLimitsRepository;

    public UserLimitsService(UserLimitsRepository userLimitsRepository,
        @Value("${com.tomcvt.captcha.default-user-limit.hourly-max-requests}") int defaultHourlyCaptchaLimit,
        @Value("${com.tomcvt.captcha.default-user-limit.daily-max-requests}") int defaultDailyCaptchaLimit,
        @Value("${com.tomcvt.request.default-user-limit.hourly-max-requests}") int defaultHourlyRequestLimit,
        @Value("${com.tomcvt.request.default-user-limit.daily-max-requests}") int defaultDailyRequestLimit
    ) {
        this.userLimitsRepository = userLimitsRepository;
        this.defaultHourlyCaptchaLimit = defaultHourlyCaptchaLimit;
        this.defaultDailyCaptchaLimit = defaultDailyCaptchaLimit;
        this.defaultHourlyRequestLimit = defaultHourlyRequestLimit;
        this.defaultDailyRequestLimit = defaultDailyRequestLimit;
    }

    public UserLimits createDefaultLimitsForUser(User user) {
        UserLimits limits = new UserLimits();
        limits.setUser(user);
        limits.setHourlyCaptchaLimit(defaultHourlyCaptchaLimit);
        limits.setDailyCaptchaLimit(defaultDailyCaptchaLimit);
        limits.setHourlyRequestLimit(defaultHourlyRequestLimit);
        limits.setDailyRequestLimit(defaultDailyRequestLimit);
        return userLimitsRepository.save(limits);
    }

}
