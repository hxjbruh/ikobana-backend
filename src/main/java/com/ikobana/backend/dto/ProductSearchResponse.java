package com.ikobana.backend.dto;

import com.ikobana.backend.entity.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lightweight DTO for product list and search results.
 *
 * <p>Contains only the essential fields needed for displaying a product
 * in a list or search results page. Use {@link ProductResponse} when
 * full product detail is required.
 *
 * <p>Used in: GET /products, GET /products/search?q=...
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    /** Product's unique database identifier. */
    private Long id;

    /** Stock Keeping Unit code. */
    private String sku;

    /** Display name of the product. */
    private String name;

    /** Product category. */
    private ProductCategory category;

    /** Retail selling price (Harga Jual). */
    private BigDecimal priceRetail;

    /** Current stock quantity. */
    private Integer stock;

    /** URL to the product image. Nullable. */
    private String productImageUrl;

    /** Whether the product is active and available for sale. */
    private Boolean isActive;
}
