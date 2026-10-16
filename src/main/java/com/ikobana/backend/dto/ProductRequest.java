package com.ikobana.backend.dto;

import com.ikobana.backend.entity.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a product.
 * Used as the request body for POST /products and PUT /products/{id}.
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    /** Stock Keeping Unit — must be unique across all products. */
    @NotBlank(message = "SKU tidak boleh kosong")
    @Length(min = 3, max = 50, message = "SKU harus antara 3 dan 50 karakter")
    private String sku;

    /** Display name of the product. */
    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Length(min = 3, max = 200, message = "Nama produk harus antara 3 dan 200 karakter")
    private String name;

    /** Optional detailed description of the product. */
    @Length(max = 2000, message = "Deskripsi maksimal 2000 karakter")
    private String description;

    /** Product category from the {@link ProductCategory} enum. */
    @NotNull(message = "Kategori produk tidak boleh kosong")
    private ProductCategory category;

    /** Selling price to end customers (Harga Jual). */
    @NotNull(message = "Harga retail tidak boleh kosong")
    @DecimalMin(value = "0", message = "Harga retail tidak boleh negatif")
    private BigDecimal priceRetail;

    /** Purchase/cost price (Harga Modal). */
    @NotNull(message = "Harga modal tidak boleh kosong")
    @DecimalMin(value = "0", message = "Harga modal tidak boleh negatif")
    private BigDecimal priceModal;

    /** Threshold below which a low-stock alert should be triggered. */
    @NotNull(message = "Minimum stok tidak boleh kosong")
    @jakarta.validation.constraints.Positive(message = "Minimum stok harus lebih dari 0")
    private Integer minimumStock;

    /** Optional warehouse or storage location name. */
    @Length(max = 100, message = "Nama gudang maksimal 100 karakter")
    private String warehouse;

    /** Optional URL to the product image. */
    private String productImageUrl;
}
