package com.ikobana.backend.entity;

import lombok.Getter;

/**
 * Enum representing product categories for Ikobana Frozen Food products.
 *
 * <p>Categories:
 * <ul>
 *   <li>{@link #OLAHAN_AYAM}       - Chicken-based frozen products</li>
 *   <li>{@link #OLAHAN_IKAN}       - Fish/seafood-based frozen products</li>
 *   <li>{@link #OLAHAN_DAGING}     - Meat-based frozen products</li>
 *   <li>{@link #CAMILAN_SIAP_MASAK}- Snacks and ready-to-cook products</li>
 *   <li>{@link #LAINNYA}           - Other frozen food products</li>
 * </ul>
 */
@Getter
public enum ProductCategory {

    OLAHAN_AYAM("Olahan Ayam"),
    OLAHAN_IKAN("Olahan Ikan"),
    OLAHAN_DAGING("Olahan Daging"),
    CAMILAN_SIAP_MASAK("Camilan & Siap Masak"),
    LAINNYA("Lainnya");

    // ── Fields ───────────────────────────────────────────────────────────────

    /** Human-readable display name for this category. */
    private final String displayName;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a ProductCategory with the given display name.
     *
     * @param displayName human-readable label
     */
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    // ── toString ─────────────────────────────────────────────────────────────

    /**
     * Returns the display name instead of the enum constant name.
     *
     * @return displayName (e.g. "Olahan Ayam")
     */
    @Override
    public String toString() {
        return displayName;
    }
}
