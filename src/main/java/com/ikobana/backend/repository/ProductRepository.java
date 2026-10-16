package com.ikobana.backend.repository;

import com.ikobana.backend.entity.Product;
import com.ikobana.backend.entity.ProductCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link Product} entity.
 * Provides data access operations for product catalog and inventory management.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find a product by its unique Stock Keeping Unit (SKU).
     *
     * @param sku product SKU code
     * @return Optional containing the Product if found, or empty Optional
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check whether a product exists with the given SKU.
     *
     * @param sku product SKU code
     * @return true if a product exists with this SKU, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Find all products belonging to a specific category.
     *
     * @param category product category enum
     * @return list of products in the category
     */
    List<Product> findByCategory(ProductCategory category);

    /**
     * Find products whose names contain the given keyword, ignoring case.
     *
     * @param name partial product name
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products where current stock is strictly less than the specified threshold.
     *
     * @param minimumStock stock threshold
     * @return list of products below the threshold
     */
    List<Product> findByStockLessThan(Integer minimumStock);

    /**
     * Find products filtered by active status.
     *
     * @param isActive active status flag
     * @return list of matching products
     */
    List<Product> findByIsActive(boolean isActive);

    /**
     * Search products by keyword matching against product name or SKU.
     *
     * @param keyword search keyword
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1% OR p.sku LIKE %?1%")
    List<Product> searchProducts(String keyword);

    /**
     * Find products where current stock is lower than their individual minimumStock threshold.
     *
     * @return list of low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stock < p.minimumStock")
    List<Product> findLowStockProducts();

    /**
     * Find active products ordered by newest creation date with pagination support.
     *
     * @param pageable pagination parameters
     * @return paged list of active products
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findActiveProductsOrderByNewest(Pageable pageable);
}
