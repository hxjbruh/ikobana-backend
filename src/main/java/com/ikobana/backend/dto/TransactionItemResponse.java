package com.ikobana.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO representing an individual item inside a transaction response.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionItemResponse {

    /** Line item unique identifier. */
    private Long id;

    /** Product ID referenced. */
    private Long productId;

    /** Product name snapshot at the time of purchase. */
    private String productName;

    /** Quantity ordered. */
    private Integer quantity;

    /** Price per unit snapshot at the time of purchase. */
    private BigDecimal pricePerUnit;

    /** Subtotal for this item (pricePerUnit * quantity). */
    private BigDecimal subtotal;
}
