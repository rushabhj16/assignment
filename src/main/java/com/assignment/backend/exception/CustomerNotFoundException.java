package com.assignment.backend.exception;

/**
 * Exception thrown when a Customer with a given ID is not found.
 */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
