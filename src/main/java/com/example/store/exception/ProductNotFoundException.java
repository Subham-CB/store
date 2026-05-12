package com.example.store.exception;

import java.util.Set;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with Id : " + productId);
    }

    public ProductNotFoundException(Set<Long> productIds) {
        super("Product not found with Id : " + productIds);
    }
}
