package com.tomcvt.cvtcaptcha.mappers;

import com.tomcvt.cvtcaptcha.dtos.ConsumerApiKeyResponse;
import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;

public class ApiKeyMapper {
    public static final ApiKeyMapper INSTANCE = new ApiKeyMapper();
    private ApiKeyMapper() {}

    public ConsumerApiKeyResponse toApiKeyResponse(ConsumerApiKeyData apiKeyData, User user) {
        return new ConsumerApiKeyResponse(
            apiKeyData.getLabel(),
            user.getUsername(),
            apiKeyData.getDomainUrl(),
            apiKeyData.getName(),
            apiKeyData.getApiKeyVersion(),
            apiKeyData.isRevoked()
        );
    }

    public ConsumerApiKeyResponse toApiKeyResponse(ConsumerApiKeyData apiKeyData) {
        return new ConsumerApiKeyResponse(
            apiKeyData.getLabel(),
            null,
            apiKeyData.getDomainUrl(),
            apiKeyData.getName(),
            apiKeyData.getApiKeyVersion(),
            apiKeyData.isRevoked()
        );
    }
}
