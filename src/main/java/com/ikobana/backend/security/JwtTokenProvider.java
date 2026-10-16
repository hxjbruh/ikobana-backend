package com.ikobana.backend.security;

import com.ikobana.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for generating, parsing, and validating JSON Web Tokens (JWT).
 * Uses HMAC-SHA algorithms with jjwt 0.12.x API.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForIkobanaBackendMustBeAtLeast32BytesLong}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate token from User entity.
     *
     * @param user user entity
     * @return signed JWT token string
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract user ID claim from JWT token.
     *
     * @param token JWT token
     * @return user ID Long
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return Long.parseLong(claims.get("userId").toString());
    }

    /**
     * Extract email/subject from JWT token.
     *
     * @param token JWT token
     * @return email/subject string
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract the expiration time of a JWT token.
     *
     * @param token JWT token
     * @return expiration as epoch milliseconds
     */
    public long getExpirationMillis(String token) {
        return getClaims(token).getExpiration().getTime();
    }

    /**
     * Validate JWT token structure and signature.
     *
     * @param token JWT token
     * @return true if valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
