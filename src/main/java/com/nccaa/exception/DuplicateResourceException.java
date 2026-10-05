package com.nccaa.exception;

/**
 * Thrown when an operation would violate a uniqueness/business constraint.
 * Mapped to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

}
