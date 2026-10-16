package com.ikobana.backend.service;

import com.ikobana.backend.dto.ProductRequest;
import com.ikobana.backend.dto.ProductResponse;
import com.ikobana.backend.dto.ProductSearchResponse;
import com.ikobana.backend.entity.Product;
import com.ikobana.backend.entity.ProductCategory;
import com.ikobana.backend.exception.DuplicateSkuException;
import com.ikobana.backend.exception.InsufficientStockException;
import com.ikobana.backend.exception.ProductNotFoundException;
import com.ikobana.backend.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling product catalog, inventory management, and stock updates.
 */
@Service
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves all products with pagination and sorting.
     *
     * @param page   zero-indexed page number
     * @param size   items per page
     * @param sortBy field to sort by (defaults to "id" if blank)
     * @return paged list of ProductResponse DTOs with computed fields
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortField));
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::convertToProductResponse);
    }

    /**
     * Retrieves a single product by its ID.
     *
     * @param id product database identifier
     * @return ProductResponse DTO
     * @throws ProductNotFoundException if product does not exist
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan"));
        return convertToProductResponse(product);
    }

    /**
     * Creates a new product catalog item.
     *
     * @param request creation request data
     * @return created ProductResponse DTO
     * @throws DuplicateSkuException if SKU is already in use
     */
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            log.warn("Failed to create product: Duplicate SKU '{}'", request.getSku());
            throw new DuplicateSkuException("Produk dengan SKU '" + request.getSku() + "' sudah ada");
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory() != null ? request.getCategory().name() : ProductCategory.LAINNYA.name());
        product.setPriceRetail(request.getPriceRetail());
        product.setPriceModal(request.getPriceModal());
        product.setStock(0);
        product.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 10);
        product.setWarehouse(request.getWarehouse());
        product.setProductImageUrl(request.getProductImageUrl());
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product ID: {}", savedProduct.getId());

        return convertToProductResponse(savedProduct);
    }

    /**
     * Updates an existing product catalog item.
     *
     * @param id      product ID to update
     * @param request update request data
     * @return updated ProductResponse DTO
     * @throws ProductNotFoundException if product not found
     * @throws DuplicateSkuException    if updated SKU is in use by another product
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan"));

        if (!product.getSku().equalsIgnoreCase(request.getSku())) {
            productRepository.findBySku(request.getSku()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    log.warn("Failed to update product ID {}: SKU '{}' is used by product ID {}", id, request.getSku(), existing.getId());
                    throw new DuplicateSkuException("SKU '" + request.getSku() + "' sudah digunakan oleh produk lain");
                }
            });
            product.setSku(request.getSku());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory().name());
        }
        product.setPriceRetail(request.getPriceRetail());
        product.setPriceModal(request.getPriceModal());
        if (request.getMinimumStock() != null) {
            product.setMinimumStock(request.getMinimumStock());
        }
        product.setWarehouse(request.getWarehouse());
        product.setProductImageUrl(request.getProductImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        log.info("Successfully updated product ID: {}", updatedProduct.getId());

        return convertToProductResponse(updatedProduct);
    }

    /**
     * Performs a soft delete on a product by setting isActive to false.
     *
     * @param id product ID to delete
     * @throws ProductNotFoundException if product not found
     */
    public void deleteProduct(Long id) {
        log.info("Soft deleting product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan"));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("Product ID {} successfully deactivated (soft deleted)", id);
    }

    /**
     * Searches products by keyword matching against name or SKU with pagination.
     *
     * @param keyword search keyword
     * @param page    zero-indexed page number
     * @param size    items per page
     * @return paged list of ProductSearchResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<ProductSearchResponse> searchProducts(String keyword, int page, int size) {
        List<Product> matches = productRepository.searchProducts(keyword != null ? keyword : "");
        int start = Math.min(page * size, matches.size());
        int end = Math.min(start + size, matches.size());

        List<ProductSearchResponse> dtoList = matches.subList(start, end).stream()
                .map(this::convertToProductSearchResponse)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtoList, pageable, matches.size());
    }

    /**
     * Retrieves all products where current stock is below their individual minimum stock threshold.
     *
     * @return list of low stock ProductResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Atomically updates a product's stock quantity.
     *
     * @param productId ID of product to update
     * @param quantity  change amount (+ to add stock, - to reduce stock)
     * @param reason    reason for stock adjustment
     * @throws ProductNotFoundException   if product not found
     * @throws InsufficientStockException if resulting stock would be negative
     */
    public void updateStock(Long productId, int quantity, String reason) {
        log.info("Updating stock for product ID: {} (delta: {}, reason: '{}')", productId, quantity, reason);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produk dengan ID " + productId + " tidak ditemukan"));

        int currentStock = product.getStock() != null ? product.getStock() : 0;
        int newStock = currentStock + quantity;

        if (newStock < 0) {
            log.error("Stock update rejected for product ID {}: current={}, delta={}, resulting={}", productId, currentStock, quantity, newStock);
            throw new InsufficientStockException("Stok tidak mencukupi untuk pengurangan sebesar " + Math.abs(quantity) + ". Stok saat ini: " + currentStock);
        }

        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        if (product.getMinimumStock() != null && newStock < product.getMinimumStock()) {
            log.warn("LOW STOCK ALERT: Product ID {} ('{}') has stock {} below minimum threshold {}",
                    productId, product.getName(), newStock, product.getMinimumStock());
        }

        log.info("Stock successfully updated for product ID {}: {} -> {}", productId, currentStock, newStock);
    }

    /**
     * Converts a Product entity to a full ProductResponse DTO with computed fields.
     */
    public ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);

        if (product.getCategory() != null) {
            try {
                response.setCategory(ProductCategory.valueOf(product.getCategory()));
            } catch (IllegalArgumentException e) {
                response.setCategory(ProductCategory.LAINNYA);
            }
        }

        boolean isLowStock = product.getStock() != null
                && product.getMinimumStock() != null
                && product.getStock() < product.getMinimumStock();
        response.setIsLowStock(isLowStock);

        BigDecimal priceRetail = product.getPriceRetail() != null ? product.getPriceRetail() : BigDecimal.ZERO;
        BigDecimal priceModal = product.getPriceModal() != null ? product.getPriceModal() : BigDecimal.ZERO;
        response.setMargin(priceRetail.subtract(priceModal));

        return response;
    }

    /**
     * Converts a Product entity to a lightweight ProductSearchResponse DTO.
     */
    public ProductSearchResponse convertToProductSearchResponse(Product product) {
        ProductSearchResponse response = modelMapper.map(product, ProductSearchResponse.class);
        if (product.getCategory() != null) {
            try {
                response.setCategory(ProductCategory.valueOf(product.getCategory()));
            } catch (IllegalArgumentException e) {
                response.setCategory(ProductCategory.LAINNYA);
            }
        }
        return response;
    }
}
