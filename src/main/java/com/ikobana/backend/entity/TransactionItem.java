package com.ikobana.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a single line item within a Transaction.
 * Maps to the "transaction_items" table in the database.
 *
 * <p>productId and productName are stored as snapshots to preserve
 * historical accuracy even if the original Product is later modified.
 */
@Entity
@Table(name = "transaction_items")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItem {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ── Relation to Transaction ───────────────────────────────────────────────

    /**
     * Parent transaction. FetchType.LAZY to avoid N+1 queries.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "transaction_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_transaction_items_transaction")
    )
    @ToString.Exclude
    private Transaction transaction;

    // ── Product Snapshot ──────────────────────────────────────────────────────
    // Stored as primitive values (not @ManyToOne) to preserve purchase history.

    @NotNull(message = "Product ID is required")
    @Column(nullable = false)
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Length(max = 200, message = "Product name must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String productName;

    // ── Quantity & Pricing ───────────────────────────────────────────────────

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0", message = "Price per unit cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerUnit;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0", message = "Subtotal cannot be negative")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    // ── Audit Timestamp ───────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Business Logic Methods ────────────────────────────────────────────────

    /**
     * Calculates and returns the subtotal for this line item (quantity × pricePerUnit).
     *
     * @return calculated subtotal as BigDecimal, or BigDecimal.ZERO if inputs are null
     */
    public BigDecimal calculateSubtotal() {
        if (this.quantity == null || this.pricePerUnit == null) {
            return BigDecimal.ZERO;
        }
        return this.pricePerUnit.multiply(BigDecimal.valueOf(this.quantity));
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "TransactionItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", pricePerUnit=" + pricePerUnit +
                ", subtotal=" + subtotal +
                ", createdAt=" + createdAt +
                '}';
    }
}
