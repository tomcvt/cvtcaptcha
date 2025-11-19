package com.tomcvt.cvtcaptcha.model;

import java.util.UUID;

import com.tomcvt.cvtcaptcha.enums.CaptchaStatus;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;

import jakarta.persistence.*;

@Entity
@Table(name = "captcha_data")
public class CaptchaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_id", unique = true, nullable = false, columnDefinition = "UUID")
    private UUID requestId = UUID.randomUUID();
    @Enumerated(EnumType.STRING)
    private CaptchaStatus status;
    @Enumerated(EnumType.STRING)
    private CaptchaType type;
    private String data;
    private String solution;
    private String parameters;
    private Long createdAt;
    private Long expiresAt;
    private String userIp;

    public CaptchaData() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public UUID getRequestId() {
        return requestId;
    }
    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }
    public CaptchaStatus getStatus() {
        return status;
    }
    public void setStatus(CaptchaStatus status) {
        this.status = status;
    }
    public CaptchaType getType() {
        return type;
    }
    public void setType(CaptchaType type) {
        this.type = type;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getSolution() {
        return solution;
    }
    public void setSolution(String solution) {
        this.solution = solution;
    }
    public String getParameters() {
        return parameters;
    }
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    public Long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    public String getUserIp() {
        return userIp;
    }
    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }


}
