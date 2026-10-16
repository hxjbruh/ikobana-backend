package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing the authentication response returned after a successful login or register.
 *
 * <p>Null fields are excluded from JSON serialization via {@link JsonInclude#NON_NULL}.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /** Indicates whether the authentication was successful. */
    private Boolean success;

    /** Human-readable message describing the result. */
    private String message;

    /**
     * Nested object containing authentication tokens and user information.
     * Null on failed authentication.
     */
    private AuthData data;

    // ── Static Factory Methods ─────────────────────────────────────────────

    /**
     * Creates a successful auth response with token data.
     *
     * @param message success message
     * @param data    authentication data (token + user)
     * @return populated AuthResponse
     */
    public static AuthResponse success(String message, AuthData data) {
        return new AuthResponse(true, message, data);
    }

    /**
     * Creates a failed auth response without data.
     *
     * @param message error message
     * @return AuthResponse with success=false and null data
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null);
    }
}
