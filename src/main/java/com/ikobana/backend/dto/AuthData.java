package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the authentication token data nested inside {@link AuthResponse}.
 *
 * <p>Null fields (e.g. {@code refreshToken}) are excluded from JSON output.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthData {

    /** JWT access token for authenticating subsequent API requests. */
    private String token;

    /**
     * JWT refresh token for obtaining a new access token.
     * Null if refresh token feature is disabled.
     */
    private String refreshToken;

    /** Public user information for the authenticated user. */
    private UserDTO user;

    /**
     * Token expiration duration in seconds.
     * (e.g. 86400 = 24 hours)
     */
    private Long expiresIn;

    // ── Static Factory Method ──────────────────────────────────────────────

    /**
     * Creates an AuthData instance without a refresh token.
     *
     * @param token     JWT access token
     * @param user      authenticated user's public data
     * @param expiresIn token lifetime in seconds
     * @return populated AuthData
     */
    public static AuthData of(String token, UserDTO user, Long expiresIn) {
        return new AuthData(token, null, user, expiresIn);
    }

    /**
     * Creates an AuthData instance with a refresh token.
     *
     * @param token        JWT access token
     * @param refreshToken JWT refresh token
     * @param user         authenticated user's public data
     * @param expiresIn    token lifetime in seconds
     * @return populated AuthData
     */
    public static AuthData of(String token, String refreshToken, UserDTO user, Long expiresIn) {
        return new AuthData(token, refreshToken, user, expiresIn);
    }
}
