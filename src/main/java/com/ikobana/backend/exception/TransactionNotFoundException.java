package com.ikobana.backend.exception;

/**
 * Exception thrown when a requested transaction cannot be found in the database.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
