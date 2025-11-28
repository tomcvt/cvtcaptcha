package com.tomcvt.cvtcaptcha.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tomcvt.cvtcaptcha.model.CaptchaData;

@Repository
public interface CaptchaDataRepository extends JpaRepository<CaptchaData, Long> {
    Optional<CaptchaData> findByRequestId(UUID requestId);
    void deleteByRequestId(UUID requestId);
}
