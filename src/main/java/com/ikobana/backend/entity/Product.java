package com.ikobana.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a frozen food product in the Ikobana system.
 * Maps to the "products" table in the database.
 */
@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_products_sku", columnNames = "sku")
    }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Identification ───────────────────────────────────────────────────────

    @NotBlank(message = "SKU is required")
    @Length(max = 50, message = "SKU must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Length(max = 200, message = "Product name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @Length(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    // ── Category ─────────────────────────────────────────────────────────────

    @NotBlank(message = "Category is required")
    @Length(max = 100, message = "Category must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String category;

    // ── Pricing ──────────────────────────────────────────────────────────────

    @NotNull(message = "Retail price is required")
    @DecimalMin(value = "0", message = "Retail price must be 0 or greater")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceRetail;

    @NotNull(message = "Modal price is required")
    @DecimalMin(value = "0", message = "Modal price must be 0 or greater")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceModal;

    // ── Inventory ─────────────────────────────────────────────────────────────

    @PositiveOrZero(message = "Stock cannot be negative")
    @Column(columnDefinition = "integer default 0")
    private Integer stock = 0;

    @PositiveOrZero(message = "Minimum stock cannot be negative")
    @Column(columnDefinition = "integer default 10")
    private Integer minimumStock = 10;

    @Length(max = 100, message = "Warehouse name must not exceed 100 characters")
    @Column(length = 100)
    private String warehouse;

    // ── Media ─────────────────────────────────────────────────────────────────

    @Column(nullable = true)
    private String productImageUrl;

    // ── Status ───────────────────────────────────────────────────────────────

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // ── Audit Timestamps ─────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ── Business Logic Methods ────────────────────────────────────────────────

    /**
     * Checks whether the current stock is below the minimum stock threshold.
     *
     * @return true if stock is less than minimumStock, false otherwise
     */
    public boolean isLowStock() {
        if (this.stock == null || this.minimumStock == null) return false;
        return this.stock < this.minimumStock;
    }

    /**
     * Calculates the profit margin per unit (priceRetail - priceModal).
     *
     * @return margin as BigDecimal, or BigDecimal.ZERO if prices are null
     */
    public BigDecimal calculateMargin() {
        if (this.priceRetail == null || this.priceModal == null) {
            return BigDecimal.ZERO;
        }
        return this.priceRetail.subtract(this.priceModal);
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", priceRetail=" + priceRetail +
                ", priceModal=" + priceModal +
                ", stock=" + stock +
                ", minimumStock=" + minimumStock +
                ", warehouse='" + warehouse + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
