package com.ikobana.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for stock adjustment requests (add or subtract stock).
 *
 * <p>Used in: PUT /api/products/{id}/stock
 *
 * <p>The {@code quantity} field is a delta: positive adds stock, negative reduces it.
 * The resulting stock may not go below zero.
 *
 * <p>Example reasons:
 * <ul>
 *   <li>"Restock dari supplier"</li>
 *   <li>"Pengembalian dari customer"</li>
 *   <li>"Stok rusak/expired"</li>
 *   <li>"Penyesuaian stok manual"</li>
 * </ul>
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockRequest {

    /**
     * Stock change: positive to add, negative to reduce. Must not be zero.
     */
    @NotNull(message = "Jumlah stok tidak boleh kosong")
    private Integer quantity;

    @AssertTrue(message = "Jumlah perubahan stok tidak boleh 0")
    private boolean isQuantityNonZero() {
        return quantity == null || quantity != 0;
    }

    /**
     * Optional note explaining the reason for the stock adjustment.
     * Useful for audit/history tracking.
     * Example: "Restock dari supplier", "Pengembalian dari customer"
     */
    @NotBlank(message = "Alasan penyesuaian stok tidak boleh kosong")
    @Length(max = 255, message = "Alasan maksimal 255 karakter")
    private String reason;
}
