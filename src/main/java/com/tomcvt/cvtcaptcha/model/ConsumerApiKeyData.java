package com.tomcvt.cvtcaptcha.model;

import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "consumer_api_keys", indexes = {
        @Index(name = "idx_consumer_api_key_hash", columnList = "apiKeyHash"),
        @Index(name = "idx_consumer_api_key_user_id", columnList = "user_id")
})
public class ConsumerApiKeyData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String apiKeyHash;
    private String apiKeyVersion;
    private String label;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String name;
    private String domainUrl;
    private boolean revoked = false;
    // TODO implement domain validation

    public ConsumerApiKeyData() {
    }

    public ConsumerApiKeyData(String apiKey, String apiKeyHash, String apiKeyVersion, User user, String name,
            String domainUrl) {
        this.apiKeyHash = apiKeyHash;
        this.apiKeyVersion = apiKeyVersion;
        this.label = createLabelForApiKey(apiKey);
        this.user = user;
        this.name = name;
        this.domainUrl = domainUrl;
    }

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

    public String getApiKeyVersion() {
        return apiKeyVersion;
    }

    public void setApiKeyVersion(String apiKeyVersion) {
        this.apiKeyVersion = apiKeyVersion;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    private static String createLabelForApiKey(String key) {
        StringBuilder sb = new StringBuilder();
        sb.append(key.substring(0, 11));
        sb.append(" **** ");
        sb.append(key.substring(key.length() - 4));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ConsumerApiKeyData [id=" + id + ", apiKeyHash=" + "****" + ", apiKeyVersion=" + apiKeyVersion
                + ", label=" + label + ", user=[proxy], name=" + name + ", domainUrl=" + domainUrl + ", revoked="
                + revoked + "]";
    }
}
