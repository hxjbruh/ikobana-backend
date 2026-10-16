package com.ikobana.backend.exception;

/**
 * Exception thrown when a product does not have sufficient stock to fulfill
 * a transaction or stock reduction request.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
