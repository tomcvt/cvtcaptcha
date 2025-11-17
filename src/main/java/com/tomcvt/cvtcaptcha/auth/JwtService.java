package com.tomcvt.cvtcaptcha.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.InvalidTokenException;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final String SECRET_KEY = "your-256-bit-secret-11111111111111111111111111111111111111111111111111111111111111";
    private final SecretKey key;
    private final JwtParser jwtParser;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        this.jwtParser = Jwts.parser().verifyWith(key).build();
    }


    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
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
            throw new InvalidTokenException("Invalid JWT token", e);
        }
    }

}
