package com.example.store.service;

import com.example.store.dto.product.ProductDTO;
import com.example.store.dto.product.ProductRequestDTO;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductDTO> findAllProducts(Pageable pageable);

    ProductDTO findProductById(Long id);

    ProductDTO createProduct(@Valid ProductRequestDTO productRequestDTO);

    void clearProductsCache();
}
