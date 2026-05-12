package com.example.store.controller;

import com.example.store.api.ProductApi;
import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
import com.example.store.api.model.SortEnumDTO;
import com.example.store.component.GlobalSearchDefaults;
import com.example.store.service.ProductService;
import com.example.store.util.PageableBuilder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductService productService;
    private final PageableBuilder pageableBuilder;
    private final GlobalSearchDefaults globalSearchDefaults;

    @Override
    public ResponseEntity<List<ProductDTO>> getProducts(
            Integer page, Integer limit, String sortBy, SortEnumDTO sortDir) {

        Pageable pageable = pageableBuilder.buildPageable(
                page,
                limit,
                sortBy,
                sortDir,
                globalSearchDefaults.getLimit(),
                globalSearchDefaults.getSortField(),
                globalSearchDefaults.getDirection());

        return ResponseEntity.status(HttpStatus.OK).body(productService.findAllProducts(pageable));
    }

    @Override
    public ResponseEntity<ProductDTO> getProductById(Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findProductById(id));
    }

    @Override
    public ResponseEntity<ProductDTO> createProduct(ProductRequestDTO productRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequestDTO));
    }
}
