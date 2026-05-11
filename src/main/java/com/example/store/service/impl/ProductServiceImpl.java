package com.example.store.service.impl;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import com.example.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Cacheable(
            value = "products",
            key = "'all_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_sort_' + #pageable.sort"
    )
    @Override
    public List<ProductDTO> findAllProducts(final Pageable pageable) {
        return productMapper.productsToProductDTOs(productRepository.findAll(pageable).getContent());
    }

    @Cacheable(
            value = "products",
            key = "'Id_' + #id"
    )
    @Override
    public ProductDTO findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException(id));
        return productMapper.productToProductDTO(product);
    }
}
