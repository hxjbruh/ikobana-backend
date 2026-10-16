package com.ikobana.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing an individual item inside a transaction creation request.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemRequest {

    /** Product ID to purchase. */
    @NotNull(message = "Product ID tidak boleh kosong")
    @Positive(message = "Product ID harus bernilai positif")
    private Long productId;

    /** Quantity of the product. */
    @NotNull(message = "Quantity tidak boleh kosong")
    @Positive(message = "Quantity harus lebih dari 0")
    private Integer quantity;
}
