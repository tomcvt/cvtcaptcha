package com.tomcvt.cvtcaptcha.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;

@Repository
public interface ConsumerApiKeyRepository extends JpaRepository<ConsumerApiKeyData, UUID> {
    Optional<ConsumerApiKeyData> findByApiKeyHash(String apiKeyHash);
    List<ConsumerApiKeyData> findByUserId(Long userId);
}
