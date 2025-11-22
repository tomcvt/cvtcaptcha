package com.tomcvt.cvtcaptcha.model;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "consumer_api_keys")
public class ConsumerApiKeyData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String apiKeyHash;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String name;
    private String domainUrl;
    //TODO implement domain validation

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getApiKeyHash() {
        return apiKeyHash;
    }
    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDomainUrl() {
        return domainUrl;
    }
    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }
}
