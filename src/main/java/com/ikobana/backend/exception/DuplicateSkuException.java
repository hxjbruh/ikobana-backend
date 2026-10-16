package com.ikobana.backend.exception;

/**
 * Exception thrown when attempting to create or update a product with an SKU
 * that already exists in the catalog.
 */
public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String message) {
        super(message);
    }

    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }
}
