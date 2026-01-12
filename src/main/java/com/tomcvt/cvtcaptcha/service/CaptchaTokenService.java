package com.tomcvt.cvtcaptcha.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.ExpiredCaptchaTokenException;
import com.tomcvt.cvtcaptcha.exceptions.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class CaptchaTokenService {
    private static final Logger log = LoggerFactory.getLogger(CaptchaTokenService.class);
    private final SecretKey cvtCaptchaKey;
    private final JwtParser jwtParser;
    private final long expirationMs;

    public CaptchaTokenService(@Value("${com.tomcvt.cvt-token.secret}") String cvtTokenSecret,
            @Value("${com.tomcvt.cvt-token.expiration-ms}") long expirationMs) {
        this.cvtCaptchaKey = Keys.hmacShaKeyFor(cvtTokenSecret.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(cvtCaptchaKey).build();
        this.expirationMs = expirationMs;
    }

    public String generateCaptchaToken(String requestId) {
        return Jwts.builder()
            .claim("requestId", requestId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(cvtCaptchaKey)
            .compact();
    }

    public String validateCaptchaToken(String token) throws InvalidTokenException {
        var claims = parseToken(token);
        var expiration = claims.getPayload().getExpiration();
        if (expiration.before(new Date())) {
            throw new ExpiredCaptchaTokenException("Captcha token has expired");
        }
        return claims.getPayload().get("requestId", String.class);
    }


    private Jws<Claims> parseToken(String token) throws InvalidTokenException {
        try {
            return jwtParser.parseSignedClaims(token);
        } catch (Exception e) {
            log.error("Invalid captcha token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid captcha token");
        }
    }
}
