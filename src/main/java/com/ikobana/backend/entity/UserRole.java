package com.ikobana.backend.entity;

import lombok.Getter;

/**
 * Enum representing user roles in the Ikobana Frozen Food system.
 *
 * <p>Roles:
 * <ul>
 *   <li>{@link #ADMIN}    - System administrator with full access</li>
 *   <li>{@link #RETAILER} - Retailer/reseller with limited access</li>
 *   <li>{@link #CUSTOMER} - End customer with basic access</li>
 * </ul>
 */
@Getter
public enum UserRole {

    ADMIN("Admin"),
    RETAILER("Retailer"),
    CUSTOMER("Customer");

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Human-readable display name for this role. */
    private final String displayName;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a UserRole with the given display name.
     *
     * @param displayName human-readable label
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    /**
     * Returns the display name instead of the enum constant name.
     *
     * @return displayName (e.g. "Admin")
     */
    @Override
    public String toString() {
        return displayName;
    }
}
