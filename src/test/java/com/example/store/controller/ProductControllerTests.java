package com.example.store.controller;

import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
import com.example.store.component.GlobalSearchDefaults;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.CustomerMapper;
import com.example.store.service.ProductService;
import com.example.store.util.PageableBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
@RequiredArgsConstructor
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private GlobalSearchDefaults globalSearchDefaults;

    @MockitoBean
    private PageableBuilder pageableBuilder;

    private ProductDTO productDTO;
    private Pageable mockPageable;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Mechanical Keyboard");
        productDTO.setOrderIds(Set.of(1L, 8L));

        mockPageable = PageRequest.of(0, 30);

        when(globalSearchDefaults.getLimit()).thenReturn(30);
        when(globalSearchDefaults.getSortField()).thenReturn("id");
        when(globalSearchDefaults.getDirection()).thenReturn("asc");

        when(pageableBuilder.buildPageable(any(), any(), any(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(mockPageable);
    }

    @Test
    @DisplayName("POST /product - returns 201 with created product body")
    void createProduct_validRequest_returns201() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("Mechanical Keyboard");

        when(productService.createProduct(request)).thenReturn(productDTO);

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("POST /product - returns 400 when description is missing")
    void createProduct_missingDescription_returns400() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(); // description not set

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    @DisplayName("POST /product - returns 400 when description exceeds maxLength of 225")
    void createProduct_descriptionTooLong_returns400() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("A".repeat(226)); // maxLength is 225 per OpenAPI spec

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    @DisplayName("POST /product - returns 400 when request body is missing entirely")
    void createProduct_noBody_returns400() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /product - returns 200 with list of products")
    void getProducts_returnsList() throws Exception {
        when(productService.findAllProducts(mockPageable)).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Mechanical Keyboard"));
    }

    @Test
    @DisplayName("GET /product - returns 200 with empty array when no products exist")
    void getProducts_noProducts_returnsEmptyList() throws Exception {
        when(productService.findAllProducts(mockPageable)).thenReturn(List.of());

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /product?sortDir=INVALID - returns 400 for invalid sort direction enum")
    void getProducts_invalidSortDir_returns400() throws Exception {
        mockMvc.perform(get("/product").param("sortDir", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /product/{id} - returns 200 with correct product body including orderIds")
    void getProductById_existingId_returns200() throws Exception {
        when(productService.findProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.orderIds").isArray())
                .andExpect(jsonPath("$.orderIds").isNotEmpty());
    }

    @Test
    @DisplayName("GET /product/{id} - returns 404 when product does not exist")
    void getProductById_nonExistentId_returns404() throws Exception {
        when(productService.findProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/product/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with Id : 999"));
    }
}
