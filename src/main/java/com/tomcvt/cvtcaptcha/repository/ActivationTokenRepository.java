package com.tomcvt.cvtcaptcha.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tomcvt.cvtcaptcha.model.ActivationToken;
import com.tomcvt.cvtcaptcha.model.User;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);
    @Query("""
        SELECT at FROM ActivationToken at
        WHERE at.expiresAt < :selectedTime
    """)
    List<ActivationToken> findAllExpiredTokens(Instant selectedTime);
    void deleteByToken(String token);
    void deleteByUser(User user);
}