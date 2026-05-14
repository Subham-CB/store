package com.example.store.exception;

public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String description) {
        super("A product with description '" + description + "' already exists");
    }
}
