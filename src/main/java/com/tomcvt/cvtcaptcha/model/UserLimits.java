package com.tomcvt.cvtcaptcha.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_limits", indexes = {
    @Index(name = "idx_user_limits_user_id", columnList = "user_id")
})
public class UserLimits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private Integer hourlyCaptchaLimit;
    private Integer dailyCaptchaLimit;

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
}
