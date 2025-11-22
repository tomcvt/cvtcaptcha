package com.tomcvt.cvtcaptcha.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.CaptchaLimitExceededException;
import com.tomcvt.cvtcaptcha.exceptions.RequestLimitExceededException;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;
import com.tomcvt.cvtcaptcha.repository.UserLimitsRepository;

@Service
public class UserRateLimiter {
    private final UserLimitsRepository userLimitsRepository;
    private final Map<Long, UserCounter> userCaptchaCounters;
    private final Map<Long, UserCounter> userRequestCounters;
    private final Map<Long, UserLimits> userLimitMap;
    //TODO on update on user limits update the userLimitMap
    private final long hourMillis = 3600000L;
    private final long dayMillis = 86400000L;

    public UserRateLimiter(UserLimitsRepository userLimitsRepository) {
        this.userCaptchaCounters = new ConcurrentHashMap<>();
        this.userRequestCounters = new ConcurrentHashMap<>();
        this.userLimitMap = new ConcurrentHashMap<>();
        this.userLimitsRepository = userLimitsRepository;
    }

    public void checkAndIncrementUserCaptchaLimit(User user) throws CaptchaLimitExceededException {
        long currentTimeMillis = System.currentTimeMillis();
        UserLimits limits = userLimitMap.get(user.getId());
        if (limits == null) {
            var dbLimits = userLimitsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User limits not found for user ID: " + user.getId()));
            //TODO if not found create default limits?
            userLimitMap.put(user.getId(), dbLimits);
            limits = dbLimits;
        }
        userCaptchaCounters.compute(user.getId(), (userId, counter) -> {
            if (counter == null) {
                return new UserCounter(currentTimeMillis);
            } else {
                // Hourly limit check
                if (currentTimeMillis - counter.getHourWindowStartMillis() > hourMillis) {
                    counter.resetHourCount(currentTimeMillis);
                } else {
                    counter.incrementHourCount();
                }
                // Daily limit check
                if (currentTimeMillis - counter.getDayWindowStartMillis() > dayMillis) {
                    counter.resetDayCount(currentTimeMillis);
                } else {
                    counter.incrementDayCount();
                }
                return counter;
            }
        });
        UserCounter counter = userCaptchaCounters.get(user.getId());
        if (counter.getHourCount() > limits.getHourlyCaptchaLimit()) {
            throw new CaptchaLimitExceededException("Hourly captcha limit exceeded for user ID: " + user.getId());
        }
        if (counter.getDayCount() > limits.getDailyCaptchaLimit()) {
            throw new CaptchaLimitExceededException("Daily captcha limit exceeded for user ID: " + user.getId());
        }
    }

    public void checkAndIncrementUserRequestLimit(User user) throws RequestLimitExceededException {
        long currentTimeMillis = System.currentTimeMillis();
        UserLimits limits = userLimitMap.get(user.getId());
        if (limits == null) {
            var dbLimits = userLimitsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User limits not found for user ID: " + user.getId()));
            userLimitMap.put(user.getId(), dbLimits);
            limits = dbLimits;
        }
        userRequestCounters.compute(user.getId(), (userId, counter) -> {
            if (counter == null) {
                return new UserCounter(currentTimeMillis);
            } else {
                // Hourly limit check
                if (currentTimeMillis - counter.getHourWindowStartMillis() > hourMillis) {
                    counter.resetHourCount(currentTimeMillis);
                } else {
                    counter.incrementHourCount();
                }
                // Daily limit check
                if (currentTimeMillis - counter.getDayWindowStartMillis() > dayMillis) {
                    counter.resetDayCount(currentTimeMillis);
                } else {
                    counter.incrementDayCount();
                }
                return counter;
            }
        });
        UserCounter counter = userRequestCounters.get(user.getId());
        if (counter.getHourCount() > limits.getHourlyRequestLimit()) {
            throw new RequestLimitExceededException("Hourly request limit exceeded for user ID: " + user.getId());
        }
        if (counter.getDayCount() > limits.getDailyRequestLimit()) {
            throw new RequestLimitExceededException("Daily request limit exceeded for user ID: " + user.getId());
        }
    }

    // TODO maybe log the username not just the id
}
