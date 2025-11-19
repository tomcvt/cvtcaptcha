package com.tomcvt.cvtcaptcha.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tomcvt.cvtcaptcha.model.CaptchaData;

public interface CaptchaDataRepository extends JpaRepository<CaptchaData, Long> {
    Optional<CaptchaData> findByRequestId(UUID requestId);
}
