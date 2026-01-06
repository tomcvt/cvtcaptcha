package com.tomcvt.cvtcaptcha.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tomcvt.cvtcaptcha.model.ConsumerApiKeyData;
import com.tomcvt.cvtcaptcha.model.User;

@Repository
public interface ConsumerApiKeyRepository extends JpaRepository<ConsumerApiKeyData, UUID> {
    Optional<ConsumerApiKeyData> findByApiKeyHash(String apiKeyHash);
    List<ConsumerApiKeyData> findByUserId(Long userId);
    List<ConsumerApiKeyData> findAllByUser(User user);
    @Query("""
        SELECT c FROM ConsumerApiKeyData c
        WHERE c.user = :user AND c.revoked = false
    """)
    List<ConsumerApiKeyData> findAllActiveByUser(@Param("user") User user);
    Optional<ConsumerApiKeyData> findByUserAndName(User user, String name);
}
