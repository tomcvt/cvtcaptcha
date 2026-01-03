package com.tomcvt.cvtcaptcha.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.InvalidTokenException;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey key;
    private final JwtParser jwtParser;
    private final long expirationMs;

    public JwtService(@Value("${com.tomcvt.cvt-token.secret}") String SECRET_KEY,
                      @Value("${com.tomcvt.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(key).build();
        this.expirationMs = expirationMs;
    }


    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = parseToken(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }

    private Jws<Claims> parseToken(String token) {
        try {
            return jwtParser.parseSignedClaims(token);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        throw new InvalidTokenException("Invalid JWT token");
    }

}
