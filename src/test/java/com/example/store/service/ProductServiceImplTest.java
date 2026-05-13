package com.example.store.service;

import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
import com.example.store.entity.Product;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import com.example.store.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setDescription("Mechanical Keyboard");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Mechanical Keyboard");

        pageable = PageRequest.of(0, 30);
    }


    @Test
    @DisplayName("findAllProducts - returns mapped list from repository page")
    void findAllProducts_returnsMappedList() {
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.productsToProductDTOs(List.of(product))).thenReturn(List.of(productDTO));

        List<ProductDTO> result = productService.findAllProducts(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getDescription()).isEqualTo("Mechanical Keyboard");

        verify(productRepository).findAll(pageable);
        verify(productMapper).productsToProductDTOs(List.of(product));
    }

    @Test
    @DisplayName("findAllProducts - returns empty list when no products exist")
    void findAllProducts_emptyPage_returnsEmptyList() {
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));
        when(productMapper.productsToProductDTOs(List.of())).thenReturn(List.of());

        List<ProductDTO> result = productService.findAllProducts(pageable);

        assertThat(result).isEmpty();
        verify(productRepository).findAll(pageable);
    }


    @Test
    @DisplayName("findProductById - returns mapped DTO when product exists")
    void findProductById_existingId_returnsMappedDTO() {
        when(productRepository.findProductById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.findProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Mechanical Keyboard");

        verify(productRepository).findProductById(1L);
        verify(productMapper).productToProductDTO(product);
    }

    @Test
    @DisplayName("findProductById - throws ProductNotFoundException when id does not exist")
    void findProductById_nonExistentId_throwsProductNotFoundException() {
        when(productRepository.findProductById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository).findProductById(999L);
        verifyNoInteractions(productMapper);
    }


    @Test
    @DisplayName("createProduct - maps request, saves entity, returns mapped DTO")
    void createProduct_savesAndReturnsMappedDTO() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("New Product");

        Product newProduct = new Product();
        newProduct.setDescription("New Product");

        Product savedProduct = new Product();
        savedProduct.setId(31L);
        savedProduct.setDescription("New Product");

        ProductDTO savedDTO = new ProductDTO();
        savedDTO.setId(31L);
        savedDTO.setDescription("New Product");

        when(productMapper.productRequestDTOToProduct(request)).thenReturn(newProduct);
        when(productRepository.save(newProduct)).thenReturn(savedProduct);
        when(productMapper.productToProductDTO(savedProduct)).thenReturn(savedDTO);

        ProductDTO result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo(31L);
        assertThat(result.getDescription()).isEqualTo("New Product");

        verify(productMapper).productRequestDTOToProduct(request);
        verify(productRepository).save(newProduct);
        verify(productMapper).productToProductDTO(savedProduct);
    }
}
