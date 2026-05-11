package com.example.store.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with Id : "+ productId);
    }
}
