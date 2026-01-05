package com.tomcvt.cvtcaptcha.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;
import com.tomcvt.cvtcaptcha.repository.UserLimitsRepository;

@Service
public class UserLimitsService {
    private final int defaultHourlyCaptchaLimit;
    private final int defaultDailyCaptchaLimit;
    private final UserLimitsRepository userLimitsRepository;

    public UserLimitsService(UserLimitsRepository userLimitsRepository,
        @Value("${com.tomcvt.captcha.default-user-limit.hourly}") int defaultHourlyCaptchaLimit,
        @Value("${com.tomcvt.captcha.default-user-limit.daily}") int defaultDailyCaptchaLimit
    ) {
        this.userLimitsRepository = userLimitsRepository;
        this.defaultHourlyCaptchaLimit = defaultHourlyCaptchaLimit;
        this.defaultDailyCaptchaLimit = defaultDailyCaptchaLimit;
    }

    @Transactional
    public UserLimits createDefaultLimitsForUser(User user) {
        UserLimits limits = new UserLimits();
        limits.setUser(user);
        limits.setHourlyCaptchaLimit(defaultHourlyCaptchaLimit);
        limits.setDailyCaptchaLimit(defaultDailyCaptchaLimit);
        return userLimitsRepository.save(limits);
    }
    @Transactional
    public UserLimits getLimitsForUser(User user) {
        var limits = userLimitsRepository.findByUser(user);
        if (limits.isPresent()) {
            return limits.get();
        } else {
            return createDefaultLimitsForUser(user);
        }
    }

    public void requestLimitsUpdate(User user) {
        // Placeholder for future implementation of dynamic limit updates
    }

    @Transactional
    public UserLimits updateUserLimits(User user, Integer hourlyCaptchaLimit, Integer dailyCaptchaLimit) {
        UserLimits limits = getLimitsForUser(user);
        if (hourlyCaptchaLimit != null) {
            limits.setHourlyCaptchaLimit(hourlyCaptchaLimit);
        }
        if (dailyCaptchaLimit != null) {
            limits.setDailyCaptchaLimit(dailyCaptchaLimit);
        }
        return userLimitsRepository.save(limits);
    }

}
