package com.tomcvt.cvtcaptcha.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tomcvt.cvtcaptcha.model.PassRecoveryToken;
import com.tomcvt.cvtcaptcha.model.User;

public interface PassRecoveryTokenRepository extends JpaRepository<PassRecoveryToken, Long> {
    Optional<PassRecoveryToken> findByToken(String token);
    @Query("""
        SELECT prt FROM PassRecoveryToken prt
        WHERE prt.expiresAt < :timestamp
    """)
    List<PassRecoveryToken> findTokensExpiredBefore(Instant timestamp);
    List<PassRecoveryToken> deleteByUser(User user);
}
