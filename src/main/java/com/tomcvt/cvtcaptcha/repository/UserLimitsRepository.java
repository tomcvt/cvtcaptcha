package com.tomcvt.cvtcaptcha.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;

public interface UserLimitsRepository extends JpaRepository<UserLimits, Long> {
    @Query("""
        SELECT ul FROM UserLimits ul WHERE ul.user = :user
            """)
    Optional<UserLimits> findByUser(@Param("user") User user);
}
