package com.ikobana.backend.controller;

import com.ikobana.backend.dto.ApiResponse;
import com.ikobana.backend.dto.ProductRequest;
import com.ikobana.backend.dto.ProductResponse;
import com.ikobana.backend.dto.ProductSearchResponse;
import com.ikobana.backend.dto.ProductStockRequest;
import com.ikobana.backend.exception.DuplicateSkuException;
import com.ikobana.backend.exception.InsufficientStockException;
import com.ikobana.backend.exception.ProductNotFoundException;
import com.ikobana.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product catalog, searching, stock adjustments, and inventory alerts.
 */
@RestController
@RequestMapping("/api/products")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all products with pagination and sorting.
     *
     * @param page   page index (default 0)
     * @param size   items per page (default 20)
     * @param sortBy property to sort by (default "createdAt")
     * @return ApiResponse containing Page of ProductResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        log.info("Fetching products list: page={}, size={}, sortBy={}", page, size, sortBy);
        Page<ProductResponse> response = productService.getAllProducts(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", response));
    }

    /**
     * Retrieves a single product by its database ID.
     *
     * @param id product identifier
     * @return ApiResponse containing ProductResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("Fetching product detail for ID: {}", id);
        try {
            ProductResponse response = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success("Product detail", response));
        } catch (ProductNotFoundException ex) {
            log.warn("Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
        }
    }

    /**
     * Searches products by keyword matching against name or SKU.
     *
     * @param keyword search keyword
     * @param page    page index (default 0)
     * @param size    items per page (default 20)
     * @return ApiResponse containing Page of ProductSearchResponse
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductSearchResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Searching products: keyword='{}', page={}, size={}", keyword, page, size);
        Page<ProductSearchResponse> response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", response));
    }

    /**
     * Retrieves list of products with inventory below minimum stock threshold.
     * Requires ADMIN role.
     *
     * @return ApiResponse containing list of low stock ProductResponse
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {
        log.info("Fetching low stock products alert list");
        List<ProductResponse> response = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success("Low stock products", response));
    }

    /**
     * Creates a new product catalog entry.
     * Requires ADMIN role.
     *
     * @param request creation request data
     * @return ApiResponse containing created ProductResponse
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody @Valid ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());
        try {
            ProductResponse response = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Product created", response));
        } catch (DuplicateSkuException ex) {
            log.warn("Product creation rejected: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("SKU already exists"));
        }
    }

    /**
     * Updates an existing product catalog item.
     * Requires ADMIN role.
     *
     * @param id      product ID to update
     * @param request updated product data
     * @return ApiResponse containing updated ProductResponse
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request
    ) {
        log.info("Updating product ID: {}", id);
        try {
            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.success("Product updated", response));
        } catch (ProductNotFoundException ex) {
            log.warn("Update rejected: Product not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
        } catch (DuplicateSkuException ex) {
            log.warn("Update rejected: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("SKU already exists"));
        }
    }

    /**
     * Soft deletes a product (sets isActive to false).
     * Requires ADMIN role.
     *
     * @param id product ID to delete
     * @return ApiResponse with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted"));
        } catch (ProductNotFoundException ex) {
            log.warn("Delete rejected: Product not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
        }
    }

    /**
     * Adjusts the stock quantity of a product.
     * Requires ADMIN role.
     *
     * @param id      product ID to update
     * @param request stock adjustment request data
     * @return ApiResponse containing updated ProductResponse
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @RequestBody @Valid ProductStockRequest request
    ) {
        log.info("Adjusting stock for product ID {}: quantity={}, reason='{}'", id, request.getQuantity(), request.getReason());
        try {
            productService.updateStock(id, request.getQuantity(), request.getReason());
            ProductResponse response = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success("Stock updated", response));
        } catch (ProductNotFoundException ex) {
            log.warn("Stock update rejected: Product not found with ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
        } catch (InsufficientStockException ex) {
            log.warn("Stock update rejected: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }
}
