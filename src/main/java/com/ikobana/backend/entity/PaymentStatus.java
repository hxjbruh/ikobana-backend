package com.ikobana.backend.entity;

import lombok.Getter;

/**
 * Enum representing the payment status of a transaction in the Ikobana system.
 *
 * <p>Lifecycle:
 * <pre>
 *   PENDING → COMPLETED
 *           → FAILED
 *           → REFUNDED (after COMPLETED)
 * </pre>
 *
 * <p>Statuses:
 * <ul>
 *   <li>{@link #PENDING}   - Awaiting payment confirmation</li>
 *   <li>{@link #COMPLETED} - Payment successfully received</li>
 *   <li>{@link #FAILED}    - Payment attempt failed</li>
 *   <li>{@link #REFUNDED}  - Payment refunded to customer</li>
 * </ul>
 */
@Getter
public enum PaymentStatus {

    PENDING("Menunggu Pembayaran"),
    COMPLETED("Pembayaran Selesai"),
    FAILED("Pembayaran Gagal"),
    REFUNDED("Pengembalian Dana");

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Human-readable display name in Bahasa Indonesia. */
    private final String displayName;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a PaymentStatus with the given display name.
     *
     * @param displayName human-readable label in Bahasa Indonesia
     */
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    /**
     * Returns the display name instead of the enum constant name.
     *
     * @return displayName (e.g. "Menunggu Pembayaran")
     */
    @Override
    public String toString() {
        return displayName;
    }
}
