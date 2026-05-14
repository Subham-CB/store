package com.example.store.service.impl;

import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
import com.example.store.entity.Product;
import com.example.store.exception.DuplicateProductException;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import com.example.store.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Cacheable(
            value = "products",
            key = "'all_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize + '_sort_' + #pageable.sort")
    @Override
    public List<ProductDTO> findAllProducts(final Pageable pageable) {
        log.debug("Fetching all products with pageable:{}", pageable);
        return productMapper.productsToProductDTOs(
                productRepository.findAll(pageable).getContent());
    }

    @Cacheable(value = "products", key = "'Id_' + #id")
    @Override
    public ProductDTO findProductById(Long id) {
        Product product = productRepository.findProductById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.productToProductDTO(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Override
    @Transactional
    public ProductDTO createProduct(final ProductRequestDTO productRequestDTO) {
        log.info("Creating product: {}", productRequestDTO.getDescription());
        if (productRepository.existsByDescriptionIgnoreCase(productRequestDTO.getDescription())) {  // <-- ADD THIS
            throw new DuplicateProductException(productRequestDTO.getDescription());                 // <-- ADD THIS
        }
        Product product = productRepository.save(productMapper.productRequestDTOToProduct(productRequestDTO));
        log.info("Product created with ID: {}", product.getId());
        return productMapper.productToProductDTO(product);
    }
}
