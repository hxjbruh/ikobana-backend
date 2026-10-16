package com.ikobana.backend.exception;

/**
 * Exception thrown when a requested user cannot be found in the database.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
