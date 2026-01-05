package com.tomcvt.cvtcaptcha.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.model.UserLimits;

@Repository
public interface UserLimitsRepository extends JpaRepository<UserLimits, Long> {
    @Query("""
        SELECT ul FROM UserLimits ul WHERE ul.user = :user
            """)
    Optional<UserLimits> findByUser(@Param("user") User user);
    @Query("""
        SELECT ul FROM UserLimits ul WHERE ul.user.id = :userId
    """)
    Optional<UserLimits> findByUserId(@Param("userId") Long userId);
}
