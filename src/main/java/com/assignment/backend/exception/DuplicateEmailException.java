package com.assignment.backend.exception;

/**
 * Exception thrown when a Customer with a duplicate email address is attempted to be created or updated.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
