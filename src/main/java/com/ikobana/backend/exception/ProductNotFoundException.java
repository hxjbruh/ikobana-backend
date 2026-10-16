package com.ikobana.backend.exception;

/**
 * Exception thrown when a requested product cannot be found in the database.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
