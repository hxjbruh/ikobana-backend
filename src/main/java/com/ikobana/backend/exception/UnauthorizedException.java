package com.ikobana.backend.exception;

/**
 * Exception thrown when an authenticated user attempts to access a resource
 * that belongs to another user or requires elevated permissions.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
