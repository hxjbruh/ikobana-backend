package com.ikobana.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a user account in the Ikobana Frozen Food system.
 * Maps to the "users" table in the database.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email",    columnNames = "email"),
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_phone",    columnNames = "phone")
    }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"passwordHash"})
public class User {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Credentials ──────────────────────────────────────────────────────────

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Length(max = 100, message = "Email must not exceed 100 characters")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Username is required")
    @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Length(max = 255, message = "Password hash must not exceed 255 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String passwordHash;

    // ── Profile ──────────────────────────────────────────────────────────────

    @Length(max = 100, message = "Full name must not exceed 100 characters")
    @Column(length = 100)
    private String fullName;

    @Length(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(unique = true, length = 20)
    private String phone;

    @Column(nullable = true)
    private String profilePictureUrl;

    // ── Role & Status ─────────────────────────────────────────────────────────

    @NotNull(message = "Role is required")
    @Length(max = 20, message = "Role must not exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String role = "CUSTOMER";

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // ── Audit Timestamps ─────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void setRole(UserRole userRole) {
        this.role = userRole != null ? userRole.name() : null;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
