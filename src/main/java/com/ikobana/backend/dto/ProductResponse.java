package com.ikobana.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ikobana.backend.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for full product detail responses.
 * Includes all product fields plus computed fields (isLowStock, margin).
 *
 * <p>Used in: GET /products/{id}, POST /products, PUT /products/{id}
 *
 * <p>Computed fields are NOT stored in the database — they are calculated
 * at the service/converter layer when building this response.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    /** Product's unique database identifier. */
    private Long id;

    /** Stock Keeping Unit code. */
    private String sku;

    /** Display name of the product. */
    private String name;

    /** Detailed product description. */
    private String description;

    /** Product category. */
    private ProductCategory category;

    /** Retail selling price (Harga Jual). */
    private BigDecimal priceRetail;

    /** Purchase/cost price (Harga Modal). */
    private BigDecimal priceModal;

    /** Current stock quantity. */
    private Integer stock;

    /** Minimum stock threshold for low-stock alerts. */
    private Integer minimumStock;

    /** Warehouse or storage location name. */
    private String warehouse;

    /** URL to the product image. */
    private String productImageUrl;

    /** Whether the product is active and available for sale. */
    private Boolean isActive;

    // ── Computed Fields ───────────────────────────────────────────────────────

    /**
     * Computed: {@code true} if {@code stock < minimumStock}.
     * Calculated at the service/converter layer, not persisted to DB.
     */
    private Boolean isLowStock;

    /**
     * Computed: {@code priceRetail - priceModal}.
     * Represents the profit margin per unit.
     * Calculated at the service/converter layer, not persisted to DB.
     */
    private BigDecimal margin;

    // ── Audit Timestamps ─────────────────────────────────────────────────────

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
