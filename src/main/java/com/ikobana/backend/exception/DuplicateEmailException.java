package com.ikobana.backend.exception;

/**
 * Exception thrown when attempting to register or update a user with an email address
 * that already exists in the system.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
