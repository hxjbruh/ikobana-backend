package com.ikobana.backend.entity;

import lombok.Getter;

/**
 * Enum representing the delivery/fulfillment status of a transaction in the Ikobana system.
 *
 * <p>Lifecycle:
 * <pre>
 *   PENDING → PROCESSING → SHIPPED → DELIVERED
 *           → CANCELLED (dari PENDING atau PROCESSING)
 * </pre>
 *
 * <p>Statuses:
 * <ul>
 *   <li>{@link #PENDING}    - Order received, awaiting processing</li>
 *   <li>{@link #PROCESSING} - Order is being prepared</li>
 *   <li>{@link #SHIPPED}    - Order has been handed to courier</li>
 *   <li>{@link #DELIVERED}  - Order successfully received by customer</li>
 *   <li>{@link #CANCELLED}  - Order was cancelled</li>
 * </ul>
 */
@Getter
public enum DeliveryStatus {

    PENDING("Menunggu Proses"),
    PROCESSING("Sedang Diproses"),
    SHIPPED("Sedang Dikirim"),
    DELIVERED("Sudah Diterima"),
    CANCELLED("Dibatalkan");

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Human-readable display name in Bahasa Indonesia. */
    private final String displayName;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a DeliveryStatus with the given display name.
     *
     * @param displayName human-readable label in Bahasa Indonesia
     */
    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    /**
     * Returns the display name instead of the enum constant name.
     *
     * @return displayName (e.g. "Sedang Dikirim")
     */
    @Override
    public String toString() {
        return displayName;
    }
}
