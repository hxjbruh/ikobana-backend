package com.ikobana.backend.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of JWT tokens that were invalidated by logout.
 * Checked by {@link JwtAuthenticationFilter} on every request so a logged-out token can no longer be used.
 * Entries are kept only until the token would have expired anyway.
 */
@Component
public class TokenBlacklist {

    /** token -> epoch millis when the token expires. */
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public void add(String token, long expiresAtMillis) {
        removeExpired();
        tokens.put(token, expiresAtMillis);
    }

    public boolean contains(String token) {
        Long expiresAt = tokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    private void removeExpired() {
        long now = System.currentTimeMillis();
        tokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
