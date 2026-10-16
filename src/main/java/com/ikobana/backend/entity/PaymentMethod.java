package com.ikobana.backend.entity;

import lombok.Getter;

/**
 * Enum representing available payment methods in the Ikobana system.
 *
 * <p>Methods:
 * <ul>
 *   <li>{@link #QRIS}          - QR Code Indonesian Standard payment</li>
 *   <li>{@link #TRANSFER_BANK} - Bank transfer payment</li>
 *   <li>{@link #GOPAY}         - GoPay e-wallet payment</li>
 *   <li>{@link #SHOPEE_PAY}    - ShopeePay e-wallet payment</li>
 * </ul>
 */
@Getter
public enum PaymentMethod {

    QRIS("QRIS"),
    TRANSFER_BANK("Transfer Bank"),
    GOPAY("GoPay"),
    SHOPEE_PAY("Shopee Pay");

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Human-readable display name for this payment method. */
    private final String displayName;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a PaymentMethod with the given display name.
     *
     * @param displayName human-readable label
     */
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    /**
     * Returns the display name instead of the enum constant name.
     *
     * @return displayName (e.g. "Transfer Bank")
     */
    @Override
    public String toString() {
        return displayName;
    }
}
