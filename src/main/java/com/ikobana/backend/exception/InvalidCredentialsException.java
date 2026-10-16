package com.ikobana.backend.exception;

/**
 * Exception thrown when authentication fails due to invalid email, username, or password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
