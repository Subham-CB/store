package com.example.store.integration;

import com.example.store.api.model.ErrorResponseDTO;
import com.example.store.api.model.ProductDTO;
import com.example.store.api.model.ProductRequestDTO;
import com.example.store.integration.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CacheManager cacheManager;


    private static final Long   SEEDED_PRODUCT_ID          = 1L;
    private static final String SEEDED_PRODUCT_DESCRIPTION = "Ergonomic Steel Keyboard";


    @BeforeEach
    void evictCache() {
        Cache cache = cacheManager.getCache("products");
        if (cache != null) {
            cache.clear();
        }
    }


    @Test
    @DisplayName("POST /product - creates a new product and returns 201 with id and description")
    void createProduct_success_returns201() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("Integration Test Product");

        ResponseEntity<ProductDTO> response =
                testRestTemplate.postForEntity("/product", request, ProductDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ProductDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isGreaterThanOrEqualTo(31L); // sequence reset to 31 after seeded 1-30
        assertThat(body.getDescription()).isEqualTo("Integration Test Product");
    }

    @Test
    @DisplayName("POST /product - returns 400 with validation error when description is missing")
    void createProduct_missingDescription_returns400() {
        ProductRequestDTO request = new ProductRequestDTO(); // description not set

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/product", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponseDTO error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getValidationErrors()).containsKey("description");
    }

    @Test
    @DisplayName("POST /product - returns 400 when description exceeds maxLength of 225")
    void createProduct_descriptionTooLong_returns400() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("A".repeat(226)); // maxLength is 225 per OpenAPI spec

        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.postForEntity("/product", request, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("description");
    }


    @Test
    @DisplayName("GET /product - returns first page of 30 products (global.search.limit = 30)")
    void getProducts_returnsDefaultPageOf30() {
        ResponseEntity<ProductDTO[]> response =
                testRestTemplate.getForEntity("/product", ProductDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // global.search.limit = 30 in application.yaml — exactly matches the 30 seeded products
        assertThat(response.getBody()).hasSize(30);
        assertThat(response.getBody())
                .allSatisfy(product -> {
                    assertThat(product.getId()).isPositive();
                    assertThat(product.getDescription()).isNotBlank();
                });
    }

    @Test
    @DisplayName("GET /product - returns 400 for invalid sort direction")
    void getProducts_invalidSortDir_returns400() {
        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.getForEntity("/product?sortDir=INVALID", ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }


    @Test
    @DisplayName("GET /product/1 - returns seeded product with correct description")
    void getProductById_seededId_returnsCorrectProduct() {
        ResponseEntity<ProductDTO> response =
                testRestTemplate.getForEntity("/product/" + SEEDED_PRODUCT_ID, ProductDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProductDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(SEEDED_PRODUCT_ID);
        assertThat(body.getDescription()).isEqualTo(SEEDED_PRODUCT_DESCRIPTION);

        assertThat(body.getOrderIds()).isNotNull();
        assertThat(body.getOrderIds()).contains(1L);
    }

    @Test
    @DisplayName("GET /product/16 - Post product and get seeded product with no order mappings")
    void postProduct_getProductById_seededIdWithNoOrders_returnsEmptyOrderIds() {

        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("Test New Integration Product");

        ResponseEntity<ProductDTO> responseProduct =
                testRestTemplate.postForEntity("/product",request,ProductDTO.class);


        assertThat(responseProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseProduct.getBody()).isNotNull();

        Long id = responseProduct.getBody().getId();

        ResponseEntity<ProductDTO> response =
                testRestTemplate.getForEntity("/product/" + id, ProductDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ProductDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(id);
        assertThat(body.getDescription()).isEqualTo("Test New Integration Product");

        assertThat(body.getOrderIds()).isEmpty();
    }

    @Test
    @DisplayName("GET /product/999999 - returns 404 for non-existent product id")
    void getProductById_nonExistentId_returns404() {
        ResponseEntity<ErrorResponseDTO> response =
                testRestTemplate.getForEntity("/product/999999", ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ErrorResponseDTO error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getStatus()).isEqualTo(404);

        assertThat(error.getMessage()).contains("999999");
    }


    @Test
    @DisplayName("Redis - GET /product/{id} populates cache on first call, eviction clears it")
    void getProductById_cachingAndEviction() {
        Cache cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();

        String cacheKey = "Id_" + SEEDED_PRODUCT_ID;

        assertThat(cache.get(cacheKey)).isNull();


        ResponseEntity<ProductDTO> firstResponse =
                testRestTemplate.getForEntity("/product/" + SEEDED_PRODUCT_ID, ProductDTO.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();

        assertThat(cache.get(cacheKey)).isNotNull();

        ResponseEntity<ProductDTO> secondResponse =
                testRestTemplate.getForEntity("/product/" + SEEDED_PRODUCT_ID, ProductDTO.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().getDescription())
                .isEqualTo(firstResponse.getBody().getDescription());

        cache.evict(cacheKey);
        assertThat(cache.get(cacheKey)).isNull();
    }

    @Test
    @DisplayName("POST /product - creating a product evicts the products cache")
    void createProduct_evictsProductsCache() {
        Cache cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();


        testRestTemplate.getForEntity("/product/" + SEEDED_PRODUCT_ID, ProductDTO.class);
        String cacheKey = "Id_" + SEEDED_PRODUCT_ID;
        assertThat(cache.get(cacheKey)).isNotNull();

        ProductRequestDTO request = new ProductRequestDTO();
        request.setDescription("Cache Eviction Test Product");
        testRestTemplate.postForEntity("/product", request, ProductDTO.class);

        assertThat(cache.get(cacheKey)).isNull();
    }
}
