package com.tomcvt.cvtcaptcha.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_limits")
public class UserLimits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private Integer hourlyCaptchaLimit;
    private Integer dailyCaptchaLimit;
    private Integer hourlyRequestLimit;
    private Integer dailyRequestLimit;

    public UserLimits() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Integer getHourlyCaptchaLimit() {
        return hourlyCaptchaLimit;
    }
    public void setHourlyCaptchaLimit(Integer hourlyCaptchaLimit) {
        this.hourlyCaptchaLimit = hourlyCaptchaLimit;
    }
    public Integer getDailyCaptchaLimit() {
        return dailyCaptchaLimit;
    }
    public void setDailyCaptchaLimit(Integer dailyCaptchaLimit) {
        this.dailyCaptchaLimit = dailyCaptchaLimit;
    }
    public Integer getHourlyRequestLimit() {
        return hourlyRequestLimit;
    }
    public void setHourlyRequestLimit(Integer hourlyRequestLimit) {
        this.hourlyRequestLimit = hourlyRequestLimit;
    }
    public Integer getDailyRequestLimit() {
        return dailyRequestLimit;
    }
    public void setDailyRequestLimit(Integer dailyRequestLimit) {
        this.dailyRequestLimit = dailyRequestLimit;
    }

}
