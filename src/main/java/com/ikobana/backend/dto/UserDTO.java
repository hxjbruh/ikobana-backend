package com.ikobana.backend.dto;

import com.ikobana.backend.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO representing the public-facing user information returned in API responses.
 *
 * <p>Note: {@code passwordHash} is intentionally excluded from this DTO
 * to prevent credential leakage in API responses.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** User's unique identifier. */
    private Long id;

    /** User's email address. */
    private String email;

    /** User's username. */
    private String username;

    /** User's full name. */
    private String fullName;

    /** User's phone number. */
    private String phone;

    /** User's role in the system. */
    private UserRole role;

    /** Whether the user account is active. */
    private Boolean isActive;

    /** URL to the user's profile picture. Nullable. */
    private String profilePictureUrl;

    /** Timestamp when the user account was created. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /** Timestamp when the user account was last updated. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
